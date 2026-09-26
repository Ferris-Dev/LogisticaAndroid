package com.trackinglogistic.data.remote.dto

import kotlinx.serialization.Serializable

/** Elemento de GET /api/pilotos/{pilotoId}/paquetes: no trae teléfono ni dirección. */
@Serializable
data class PaqueteResumenDto(
    val guia: String,
    val cliente: String = "",
    val zona: String = "",
    val estado: String = "PENDIENTE",
)

/** Respuesta de GET /api/tracking/{guia} y de PUT /api/tracking/{guia}/estado. */
@Serializable
data class PaqueteDetalleDto(
    val guia: String,
    val cliente: String = "",
    val telefono: String? = null,
    val direccion: String = "",
    val zona: String = "",
    val estado: String = "PENDIENTE",
    val observaciones: String? = null,
    val pilotoId: Int? = null,
    val fechaAsignacion: String? = null,
    /** ISO-8601 UTC. Referencia del last-write-wins del servidor. */
    val ultimaActualizacion: String? = null,
)

/**
 * Cuerpo de PUT /api/tracking/{guia}/estado. Los null no se serializan (explicitNulls = false):
 * - [fechaCambio]: momento real del cambio en el dispositivo (UTC, sufijo Z). Si falta, el servidor usa su hora.
 * - [observaciones]: ausente = no modificar; "" = borrar; máximo 500 caracteres.
 */
@Serializable
data class ActualizarEstadoRequest(
    val estado: String,
    val fechaCambio: String? = null,
    val observaciones: String? = null,
)

/** Error RFC 7807 (application/problem+json). "type" y "traceId" se ignoran. */
@Serializable
data class ProblemDetailsDto(
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val errors: Map<String, List<String>>? = null,
)
