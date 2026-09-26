package com.trackinglogistic.domain.model

sealed interface ResultadoBusqueda {
    data class Encontrado(val paquete: Paquete) : ResultadoBusqueda
    /** 404. [mensaje] es el "detail" del servidor, p. ej. "La guía GUA-99999 no existe". */
    data class NoExiste(val mensaje: String) : ResultadoBusqueda

    /** Formato distinto de GUA-\d{5}: detectado localmente o por un 400 del servidor. */
    data class FormatoInvalido(val mensaje: String = MENSAJE_FORMATO) : ResultadoBusqueda
    data object SinConexion : ResultadoBusqueda
    data class Error(val mensaje: String) : ResultadoBusqueda
}

sealed interface ResultadoRefresco {
    data object Exito : ResultadoRefresco
    data object SinConexion : ResultadoRefresco

    /** 404 del listado: el piloto no existe (error de configuración, no "sin entregas"). */
    data class PilotoNoExiste(val mensaje: String) : ResultadoRefresco
    data class Error(val mensaje: String) : ResultadoRefresco
}

const val MENSAJE_FORMATO = "La guía debe tener el formato GUA-00000 (5 dígitos), por ejemplo 10001 o GUA-10001."
