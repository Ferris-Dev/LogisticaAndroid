package com.trackinglogistic.ui.paquetes

import com.trackinglogistic.domain.model.Paquete

sealed interface PaquetesUiState {
    data object Loading : PaquetesUiState

    /** [paquetes] ya viene filtrada por la consulta; puede quedar vacía si nada coincide. */
    data class Success(val paquetes: List<Paquete>, val filtrando: Boolean) : PaquetesUiState

    data class Empty(
        val titulo: String,
        val mensaje: String,
        val sinConexion: Boolean = false,
    ) : PaquetesUiState

    /** [pilotoInvalido]: 404 del listado, el código de piloto no existe (se ofrece cambiarlo). */
    data class Error(val mensaje: String, val pilotoInvalido: Boolean = false) : PaquetesUiState
}

/** Estado de la consulta al servidor cuando la guía no está en la lista local. */
sealed interface BusquedaRemotaState {
    data object Inactiva : BusquedaRemotaState
    data object Buscando : BusquedaRemotaState
    data class NoExiste(val mensaje: String) : BusquedaRemotaState
    data class FormatoInvalido(val mensaje: String) : BusquedaRemotaState
    data class Fallo(val mensaje: String) : BusquedaRemotaState
}

sealed interface PaquetesEvento {
    data class AbrirDetalle(val guia: String) : PaquetesEvento
    data class Mensaje(val texto: String) : PaquetesEvento
}
