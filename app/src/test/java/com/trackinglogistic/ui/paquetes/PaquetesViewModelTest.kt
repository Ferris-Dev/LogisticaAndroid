package com.trackinglogistic.ui.paquetes

import com.trackinglogistic.domain.model.ResultadoRefresco
import com.trackinglogistic.domain.usecase.BuscarPorGuiaUseCase
import com.trackinglogistic.domain.usecase.ObtenerPaquetesUseCase
import com.trackinglogistic.fakes.FakePaqueteRepository
import com.trackinglogistic.fakes.FakePilotoRepository
import com.trackinglogistic.fakes.MainDispatcherRule
import com.trackinglogistic.fakes.paquete
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Filtro por guía y búsqueda remota del ViewModel de la lista, con repositorios fake. */
@OptIn(ExperimentalCoroutinesApi::class)
class PaquetesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePaqueteRepository(
        listOf(paquete("GUA-10001"), paquete("GUA-10002"), paquete("GUA-20001")),
    )

    private fun crearViewModel() = PaquetesViewModel(
        pilotoRepository = FakePilotoRepository(),
        obtenerPaquetes = ObtenerPaquetesUseCase(repository),
        buscarPorGuia = BuscarPorGuiaUseCase(repository),
    )

    private fun TestScope.suscribir(viewModel: PaquetesViewModel): List<PaquetesEvento> {
        val eventos = mutableListOf<PaquetesEvento>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.eventos.collect(eventos::add) }
        return eventos
    }

    private fun PaquetesViewModel.guiasVisibles() =
        (uiState.value as PaquetesUiState.Success).paquetes.map { it.guia }

    @Test
    fun `sin consulta muestra todos los paquetes del piloto`() = runTest {
        val viewModel = crearViewModel()
        suscribir(viewModel)

        assertEquals(listOf("GUA-10001", "GUA-10002", "GUA-20001"), viewModel.guiasVisibles())
    }

    @Test
    fun `filtra localmente por numero o por codigo completo`() = runTest {
        val viewModel = crearViewModel()
        suscribir(viewModel)

        viewModel.onConsultaChange("10001")
        assertEquals(listOf("GUA-10001"), viewModel.guiasVisibles())

        viewModel.onConsultaChange("GUA-20001")
        assertEquals(listOf("GUA-20001"), viewModel.guiasVisibles())

        viewModel.onConsultaChange("")
        assertEquals(3, viewModel.guiasVisibles().size)
    }

    @Test
    fun `buscar con una sola coincidencia local abre el detalle sin consultar al API`() = runTest {
        val viewModel = crearViewModel()
        val eventos = suscribir(viewModel)

        viewModel.onConsultaChange("10002")
        viewModel.onBuscar()

        assertEquals(listOf(PaquetesEvento.AbrirDetalle("GUA-10002")), eventos)
        assertTrue(repository.guiasBuscadasRemoto.isEmpty())
    }

    @Test
    fun `sin coincidencia local consulta el API con la guia normalizada`() = runTest {
        repository.remotos = mapOf("GUA-30003" to paquete("GUA-30003"))
        val viewModel = crearViewModel()
        val eventos = suscribir(viewModel)

        viewModel.onConsultaChange("30003")
        viewModel.onBuscar()

        assertEquals(listOf("GUA-30003"), repository.guiasBuscadasRemoto)
        assertEquals(listOf(PaquetesEvento.AbrirDetalle("GUA-30003")), eventos)
    }

    @Test
    fun `404 del API muestra que la guia no existe`() = runTest {
        val viewModel = crearViewModel()
        suscribir(viewModel)

        viewModel.onConsultaChange("99999")
        viewModel.onBuscar()

        // Se muestra el "detail" del servidor.
        assertEquals(BusquedaRemotaState.NoExiste("La guía GUA-99999 no existe"), viewModel.busquedaRemota.value)
        assertEquals(emptyList<String>(), viewModel.guiasVisibles())
    }

    @Test
    fun `guia con formato invalido no consulta el API`() = runTest {
        val viewModel = crearViewModel()
        suscribir(viewModel)

        viewModel.onConsultaChange("123")
        viewModel.onBuscar()

        assertTrue(viewModel.busquedaRemota.value is BusquedaRemotaState.FormatoInvalido)
        assertTrue(repository.guiasBuscadasRemoto.isEmpty())
    }

    @Test
    fun `al editar la consulta se limpia el resultado de la busqueda remota`() = runTest {
        val viewModel = crearViewModel()
        suscribir(viewModel)
        viewModel.onConsultaChange("99999")
        viewModel.onBuscar()

        viewModel.onConsultaChange("9999")

        assertEquals(BusquedaRemotaState.Inactiva, viewModel.busquedaRemota.value)
    }

    private fun TestScope.viewModelSinDatos(refresco: ResultadoRefresco): PaquetesViewModel {
        val vacio = FakePaqueteRepository().apply { resultadoRefresco = refresco }
        return PaquetesViewModel(
            pilotoRepository = FakePilotoRepository(),
            obtenerPaquetes = ObtenerPaquetesUseCase(vacio),
            buscarPorGuia = BuscarPorGuiaUseCase(vacio),
        ).also { suscribir(it) }
    }

    @Test
    fun `piloto inexistente (404) muestra error de configuracion distinto de lista vacia`() = runTest {
        val viewModel = viewModelSinDatos(ResultadoRefresco.PilotoNoExiste("El piloto 99 no existe"))

        assertEquals(
            PaquetesUiState.Error("El piloto 99 no existe", pilotoInvalido = true),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `piloto sin entregas (200 vacio) muestra estado vacio`() = runTest {
        val viewModel = viewModelSinDatos(ResultadoRefresco.Exito)

        val estado = viewModel.uiState.value
        assertTrue(estado is PaquetesUiState.Empty && !estado.sinConexion)
        assertEquals("Sin entregas para hoy", (estado as PaquetesUiState.Empty).titulo)
    }

    @Test
    fun `sin red y sin datos locales muestra estado vacio de sin conexion`() = runTest {
        val vacio = FakePaqueteRepository().apply { resultadoRefresco = ResultadoRefresco.SinConexion }
        val viewModel = PaquetesViewModel(
            pilotoRepository = FakePilotoRepository(),
            obtenerPaquetes = ObtenerPaquetesUseCase(vacio),
            buscarPorGuia = BuscarPorGuiaUseCase(vacio),
        )
        suscribir(viewModel)

        val estado = viewModel.uiState.value
        assertTrue(estado is PaquetesUiState.Empty && estado.sinConexion)
    }
}
