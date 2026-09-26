package com.trackinglogistic.ui.detalle

import com.trackinglogistic.domain.model.EstadoPaquete
import com.trackinglogistic.domain.usecase.ActualizarEstadoUseCase
import com.trackinglogistic.domain.usecase.ObtenerPaquetesUseCase
import com.trackinglogistic.fakes.FakePaqueteRepository
import com.trackinglogistic.fakes.MainDispatcherRule
import com.trackinglogistic.fakes.paquete
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakePaqueteRepository
    private lateinit var viewModel: DetalleViewModel

    @Before
    fun setUp() {
        repository = FakePaqueteRepository(
            listOf(
                paquete("GUA-10001", EstadoPaquete.PENDIENTE),
                paquete("GUA-10002", EstadoPaquete.EN_RUTA),
            ),
        )
        viewModel = DetalleViewModel(
            obtenerPaquetes = ObtenerPaquetesUseCase(repository),
            actualizarEstado = ActualizarEstadoUseCase(repository),
        )
    }

    /** uiState usa WhileSubscribed: necesita un suscriptor activo, como lo haría la pantalla. */
    private fun TestScope.suscribir() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private fun TestScope.eventos(): List<DetalleEvento> {
        val recibidos = mutableListOf<DetalleEvento>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.eventos.collect(recibidos::add) }
        return recibidos
    }

    private val success get() = viewModel.uiState.value as DetalleUiState.Success

    @Test
    fun `muestra Loading antes de cargar una guia`() = runTest {
        suscribir()
        assertEquals(DetalleUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `cargar muestra el paquete con su estado actual preseleccionado`() = runTest {
        suscribir()
        viewModel.cargar("GUA-10001")

        assertEquals("GUA-10001", success.paquete.guia)
        assertEquals(EstadoPaquete.PENDIENTE, success.estadoSeleccionado)
    }

    @Test
    fun `al abrir una guia se descarga su detalle completo del API`() = runTest {
        suscribir()
        viewModel.cargar("GUA-10001")
        viewModel.cargar("GUA-10001") // recomponer no vuelve a descargar

        assertEquals(listOf("GUA-10001"), repository.detallesRefrescados)
        assertFalse(success.cargandoDetalle)
    }

    @Test
    fun `guia inexistente en Room muestra Empty`() = runTest {
        suscribir()
        viewModel.cargar("GUA-99999")

        assertTrue(viewModel.uiState.value is DetalleUiState.Empty)
    }

    @Test
    fun `boton deshabilitado si el estado seleccionado no cambio`() = runTest {
        suscribir()
        viewModel.cargar("GUA-10001")
        assertFalse(success.puedeActualizar)

        viewModel.onEstadoSeleccionado(EstadoPaquete.PENDIENTE)
        assertFalse(success.puedeActualizar)
    }

    @Test
    fun `boton habilitado al elegir un estado distinto`() = runTest {
        suscribir()
        viewModel.cargar("GUA-10001")
        viewModel.onEstadoSeleccionado(EstadoPaquete.ENTREGADO)

        assertTrue(success.puedeActualizar)
    }

    @Test
    fun `actualizar guarda el estado, programa sync y emite snackbar de confirmacion`() = runTest {
        suscribir()
        val eventos = eventos()
        viewModel.cargar("GUA-10001")
        viewModel.onEstadoSeleccionado(EstadoPaquete.ENTREGADO)

        viewModel.onActualizar()

        assertEquals(listOf("GUA-10001" to EstadoPaquete.ENTREGADO), repository.actualizaciones)
        assertEquals(1, repository.sincronizacionesProgramadas)
        assertEquals(EstadoPaquete.ENTREGADO, success.paquete.estado)
        assertTrue(success.paquete.syncPendiente)
        // Tras guardar, el estado actual coincide con el seleccionado: el botón vuelve a deshabilitarse.
        assertFalse(success.puedeActualizar)
        assertEquals(listOf(DetalleEvento.Mensaje("Estado actualizado a «Entregado»")), eventos)
    }

    @Test
    fun `las observaciones se envian con el cambio y el campo se limpia`() = runTest {
        suscribir()
        viewModel.cargar("GUA-10001")
        viewModel.onEstadoSeleccionado(EstadoPaquete.NO_ENTREGADO)
        viewModel.onObservacionesChange("  Local cerrado ")

        viewModel.onActualizar()

        assertEquals(listOf<String?>("Local cerrado"), repository.observacionesEnviadas)
        assertEquals("", success.observaciones)
    }

    @Test
    fun `observaciones vacias no se envian (no modifican las del servidor)`() = runTest {
        suscribir()
        viewModel.cargar("GUA-10001")
        viewModel.onEstadoSeleccionado(EstadoPaquete.ENTREGADO)
        viewModel.onObservacionesChange("   ")

        viewModel.onActualizar()

        assertEquals(listOf<String?>(null), repository.observacionesEnviadas)
    }

    @Test
    fun `si el servidor impone otro estado la seleccion vuelve al estado vigente`() = runTest {
        suscribir()
        viewModel.cargar("GUA-10001")
        viewModel.onEstadoSeleccionado(EstadoPaquete.EN_RUTA)

        // Last-write-wins: el servidor responde con un estado más reciente distinto.
        repository.imponerEstadoDelServidor("GUA-10001", EstadoPaquete.ENTREGADO)

        assertEquals(EstadoPaquete.ENTREGADO, success.paquete.estado)
        assertEquals(EstadoPaquete.ENTREGADO, success.estadoSeleccionado)
        assertFalse(success.puedeActualizar)
    }

    @Test
    fun `actualizar sin cambios no llama al repositorio`() = runTest {
        suscribir()
        viewModel.cargar("GUA-10001")

        viewModel.onActualizar()

        assertTrue(repository.actualizaciones.isEmpty())
    }

    @Test
    fun `si falla el guardado se informa y se restablece la seleccion`() = runTest {
        suscribir()
        val eventos = eventos()
        repository.fallarActualizacion = true
        viewModel.cargar("GUA-10001")
        viewModel.onEstadoSeleccionado(EstadoPaquete.NO_ENTREGADO)

        viewModel.onActualizar()

        assertEquals(EstadoPaquete.PENDIENTE, success.paquete.estado)
        assertEquals(EstadoPaquete.PENDIENTE, success.estadoSeleccionado)
        assertFalse(success.actualizando)
        assertEquals(1, eventos.size)
    }

    @Test
    fun `cambiar de guia descarta la seleccion anterior`() = runTest {
        suscribir()
        viewModel.cargar("GUA-10001")
        viewModel.onEstadoSeleccionado(EstadoPaquete.ENTREGADO)

        viewModel.cargar("GUA-10002")

        assertEquals("GUA-10002", success.paquete.guia)
        assertEquals(EstadoPaquete.EN_RUTA, success.estadoSeleccionado)
        assertFalse(success.puedeActualizar)
    }
}
