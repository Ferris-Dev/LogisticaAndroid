package com.trackinglogistic.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackinglogistic.domain.model.EstadoPaquete
import com.trackinglogistic.domain.model.Paquete
import com.trackinglogistic.domain.usecase.ActualizarEstadoUseCase
import com.trackinglogistic.domain.usecase.ObtenerPaquetesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetalleUiState {
    data object Loading : DetalleUiState

    data class Success(
        val paquete: Paquete,
        val estadoSeleccionado: EstadoPaquete,
        val actualizando: Boolean,
        /** Descargando teléfono/dirección del API; la pantalla ya muestra lo que hay en Room. */
        val cargandoDetalle: Boolean = false,
        /** Texto del campo de observaciones (motivo), se envía junto con el cambio de estado. */
        val observaciones: String = "",
    ) : DetalleUiState {
        /** El botón ACTUALIZAR ESTADO solo se habilita si el piloto eligió un estado distinto. */
        val puedeActualizar: Boolean get() = !actualizando && estadoSeleccionado != paquete.estado
    }

    data class Empty(val mensaje: String) : DetalleUiState
    data class Error(val mensaje: String) : DetalleUiState
}

sealed interface DetalleEvento {
    data class Mensaje(val texto: String) : DetalleEvento
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetalleViewModel @Inject constructor(
    private val obtenerPaquetes: ObtenerPaquetesUseCase,
    private val actualizarEstado: ActualizarEstadoUseCase,
) : ViewModel() {

    private val guia = MutableStateFlow<String?>(null)

    /** Estado elegido en el dropdown; null = aún no ha elegido (se muestra el actual). */
    private val seleccion = MutableStateFlow<EstadoPaquete?>(null)
    private val actualizando = MutableStateFlow(false)
    private val cargandoDetalle = MutableStateFlow(false)
    private val observaciones = MutableStateFlow("")
    private var descargaDetalle: Job? = null

    private val _eventos = Channel<DetalleEvento>(Channel.BUFFERED)
    val eventos: Flow<DetalleEvento> = _eventos.receiveAsFlow()

    val uiState: StateFlow<DetalleUiState> =
        combine(
            guia.filterNotNull().flatMapLatest { obtenerPaquetes.porGuia(it) },
            seleccion,
            actualizando,
            cargandoDetalle,
            observaciones,
        ) { actual, elegido, enCurso, descargando, nota ->
            if (actual == null) {
                DetalleUiState.Empty("La guía no está disponible en este dispositivo")
            } else {
                DetalleUiState.Success(
                    paquete = actual,
                    estadoSeleccionado = elegido ?: actual.estado,
                    actualizando = enCurso,
                    cargandoDetalle = descargando,
                    observaciones = nota,
                )
            }
        }
            .catch<DetalleUiState> { emit(DetalleUiState.Error("No se pudo cargar el paquete")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUiState.Loading)

    init {
        // Cuando cambia el estado real (tras guardar, o porque el servidor impuso uno más reciente),
        // la selección vuelve al estado actual: el botón se deshabilita y no se puede reenviar
        // un cambio que el servidor ya descartó.
        guia.filterNotNull()
            .flatMapLatest { obtenerPaquetes.porGuia(it) }
            .map { it?.estado }
            .distinctUntilChanged()
            .onEach { seleccion.value = null }
            .launchIn(viewModelScope)
    }

    fun cargar(guia: String) {
        if (this.guia.value == guia) return
        seleccion.value = null
        observaciones.value = ""
        this.guia.value = guia
        // Room se muestra de inmediato; en paralelo se trae el detalle actualizado del API.
        // Sin red no pasa nada: queda lo que haya en Room.
        descargaDetalle?.cancel()
        descargaDetalle = viewModelScope.launch {
            cargandoDetalle.value = true
            try {
                obtenerPaquetes.refrescarDetalle(guia)
            } finally {
                cargandoDetalle.value = false
            }
        }
    }

    fun onEstadoSeleccionado(estado: EstadoPaquete) {
        seleccion.value = estado
    }

    fun onObservacionesChange(texto: String) {
        observaciones.value = texto.take(ActualizarEstadoUseCase.MAX_OBSERVACIONES)
    }

    fun onActualizar() {
        val estado = uiState.value as? DetalleUiState.Success ?: return
        if (!estado.puedeActualizar) return
        val nuevo = estado.estadoSeleccionado
        viewModelScope.launch {
            actualizando.value = true
            try {
                actualizarEstado(estado.paquete.guia, nuevo, estado.observaciones)
                observaciones.value = ""
                _eventos.send(DetalleEvento.Mensaje("Estado actualizado a «${nuevo.etiqueta}»"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                seleccion.value = null
                _eventos.send(DetalleEvento.Mensaje("No se pudo guardar el cambio. Intenta de nuevo."))
            } finally {
                actualizando.value = false
            }
        }
    }
}
