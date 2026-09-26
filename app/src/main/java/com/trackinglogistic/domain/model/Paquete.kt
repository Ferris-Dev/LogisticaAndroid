package com.trackinglogistic.domain.model

data class Paquete(
    val guia: String,
    val cliente: String,
    val telefono: String,
    val direccion: String,
    val zona: String,
    val estado: EstadoPaquete,
    val syncPendiente: Boolean = false,
    val observaciones: String? = null,
    /**
     * false si solo se tiene el resumen de la lista (sin teléfono ni dirección) y el
     * detalle aún no se ha podido descargar.
     */
    val detalleCargado: Boolean = true,
    /** ISO-8601 UTC del servidor; se convierte a hora local solo para mostrarla. */
    val ultimaActualizacion: String? = null,
    /** Aviso cuando el servidor no aplicó el último cambio del piloto (superado o rechazado). */
    val avisoSync: String? = null,
) {
    /** Parte numérica de la guía ("GUA-10001" → "10001"), usada para mostrar "#10001". */
    val numero: String get() = Guia.numero(guia)
}
