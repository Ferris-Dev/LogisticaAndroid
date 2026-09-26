package com.trackinglogistic.ui.paquetes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackinglogistic.domain.model.Paquete
import com.trackinglogistic.domain.model.ResultadoBusqueda
import com.trackinglogistic.domain.model.ResultadoRefresco
import com.trackinglogistic.domain.repository.PilotoRepository
import com.trackinglogistic.domain.usecase.BuscarPorGuiaUseCase
import com.trackinglogistic.domain.usecase.ObtenerPaquetesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PaquetesViewModel @Inject constructor(
    private val pilotoRepository: PilotoRepository,
    private val obtenerPaquetes: ObtenerPaquetesUseCase,
    private val buscarPorGuia: BuscarPorGuiaUseCase,
) : ViewModel() {

    val pilotoId: StateFlow<String?> = pilotoRepository.pilotoId
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> = _consulta.asStateFlow()

    private val _refrescando = MutableStateFlow(false)
    val refrescando: StateFlow<Boolean> = _refrescando.asStateFlow()

    private val _busquedaRemota = MutableStateFlow<BusquedaRemotaState>(BusquedaRemotaState.Inactiva)
    val busquedaRemota: StateFlow<BusquedaRemotaState> = _busquedaRemota.asStateFlow()

    private val _eventos = Channel<PaquetesEvento>(Channel.BUFFERED)
    val eventos: Flow<PaquetesEvento> = _eventos.receiveAsFlow()

    /** Resultado del último refresco; null mientras no ha terminado el primero. */
    private val ultimoRefresco = MutableStateFlow<ResultadoRefresco?>(null)

    /** Lista completa del piloto desde Room (fuente de verdad); null hasta la primera lectura. */
    private val paquetesLocales: StateFlow<List<Paquete>?> = pilotoRepository.pilotoId
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { obtenerPaquetes(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiState: StateFlow<PaquetesUiState> =
        combine(paquetesLocales, _consulta, ultimoRefresco) { paquetes, consulta, refresco ->
            when {
                paquetes == null -> PaquetesUiState.Loading
                paquetes.isNotEmpty() -> PaquetesUiState.Success(
                    paquetes = buscarPorGuia.filtrarLocal(paquetes, consulta),
                    filtrando = consulta.isNotBlank(),
                )
                // Room vacío: el mensaje depende de lo que respondió la red.
                refresco == null -> PaquetesUiState.Loading
                refresco is ResultadoRefresco.SinConexion -> PaquetesUiState.Empty(
                    titulo = "Sin conexión",
                    mensaje = "No hay paquetes guardados en este dispositivo. " +
                        "Conéctate a internet y desliza hacia abajo para descargarlos.",
                    sinConexion = true,
                )
                refresco is ResultadoRefresco.PilotoNoExiste ->
                    PaquetesUiState.Error(refresco.mensaje, pilotoInvalido = true)
                refresco is ResultadoRefresco.Error -> PaquetesUiState.Error(refresco.mensaje)
                // 200 []: el piloto existe pero hoy no tiene entregas.
                else -> PaquetesUiState.Empty(
                    titulo = "Sin entregas para hoy",
                    mensaje = "No tienes paquetes asignados por ahora. Desliza hacia abajo para actualizar.",
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PaquetesUiState.Loading)

    init {
        // Al abrir (o al cambiar de piloto): se muestra Room y en paralelo se refresca desde el API.
        viewModelScope.launch {
            pilotoRepository.pilotoId.filterNotNull().distinctUntilChanged().collect { id ->
                ultimoRefresco.value = null
                refrescar(id, avisarFallo = false)
            }
        }
    }

    fun onRefresh() {
        val id = pilotoId.value ?: return
        viewModelScope.launch { refrescar(id, avisarFallo = true) }
    }

    private suspend fun refrescar(pilotoId: String, avisarFallo: Boolean) {
        _refrescando.value = true
        val resultado = obtenerPaquetes.refrescar(pilotoId)
        ultimoRefresco.value = resultado
        _refrescando.value = false

        val hayDatosLocales = !paquetesLocales.value.isNullOrEmpty()
        if (hayDatosLocales && (avisarFallo || resultado !is ResultadoRefresco.SinConexion)) {
            when (resultado) {
                ResultadoRefresco.SinConexion ->
                    _eventos.send(PaquetesEvento.Mensaje("Sin conexión. Mostrando los datos guardados."))
                is ResultadoRefresco.Error ->
                    _eventos.send(PaquetesEvento.Mensaje("No se pudo actualizar: ${resultado.mensaje}"))
                is ResultadoRefresco.PilotoNoExiste -> _eventos.send(PaquetesEvento.Mensaje(resultado.mensaje))
                ResultadoRefresco.Exito -> Unit
            }
        }
    }

    fun onConsultaChange(texto: String) {
        _consulta.value = texto
        _busquedaRemota.value = BusquedaRemotaState.Inactiva
    }

    /** Buscar: si la guía no está en la lista local, se consulta GET /api/tracking/{guia}. */
    fun onBuscar() {
        val consulta = _consulta.value
        if (consulta.isBlank() || _busquedaRemota.value == BusquedaRemotaState.Buscando) return

        val locales = buscarPorGuia.filtrarLocal(paquetesLocales.value.orEmpty(), consulta)
        if (locales.size == 1) {
            _eventos.trySend(PaquetesEvento.AbrirDetalle(locales.first().guia))
            return
        }
        if (locales.isNotEmpty()) return // La lista ya muestra las coincidencias.

        viewModelScope.launch {
            _busquedaRemota.value = BusquedaRemotaState.Buscando
            _busquedaRemota.value = when (val resultado = buscarPorGuia.buscarRemoto(consulta)) {
                is ResultadoBusqueda.Encontrado -> {
                    _eventos.send(PaquetesEvento.AbrirDetalle(resultado.paquete.guia))
                    BusquedaRemotaState.Inactiva
                }
                is ResultadoBusqueda.NoExiste -> BusquedaRemotaState.NoExiste(resultado.mensaje)
                is ResultadoBusqueda.FormatoInvalido -> BusquedaRemotaState.FormatoInvalido(resultado.mensaje)
                ResultadoBusqueda.SinConexion ->
                    BusquedaRemotaState.Fallo("Sin conexión: no se pudo consultar la guía en el servidor.")
                is ResultadoBusqueda.Error -> BusquedaRemotaState.Fallo(resultado.mensaje)
            }
        }
    }

    fun onCambiarPiloto() {
        viewModelScope.launch { pilotoRepository.cerrarSesion() }
    }
}
