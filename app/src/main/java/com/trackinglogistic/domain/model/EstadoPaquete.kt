package com.trackinglogistic.domain.model

enum class EstadoPaquete(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_RUTA("En ruta"),
    ENTREGADO("Entregado"),
    NO_ENTREGADO("No entregado");

    companion object {
        /** Tolera variaciones del API ("en_ruta", "EN RUTA", "En-Ruta"). Desconocido → PENDIENTE. */
        fun desdeApi(valor: String?): EstadoPaquete {
            val normalizado = valor.orEmpty().trim().uppercase().replace(' ', '_').replace('-', '_')
            return entries.firstOrNull { it.name == normalizado } ?: PENDIENTE
        }
    }
}
