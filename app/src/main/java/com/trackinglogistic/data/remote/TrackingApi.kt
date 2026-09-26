package com.trackinglogistic.data.remote

import com.trackinglogistic.data.remote.dto.ActualizarEstadoRequest
import com.trackinglogistic.data.remote.dto.PaqueteDetalleDto
import com.trackinglogistic.data.remote.dto.PaqueteResumenDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Contrato del backend. Los errores 4xx llegan como ProblemDetails (RFC 7807);
 * la app solo necesita el código HTTP, por eso no se deserializa el cuerpo de error.
 */
interface TrackingApi {

    /** pilotoId numérico: PIL-001 → 1. */
    @GET("api/pilotos/{pilotoId}/paquetes")
    suspend fun obtenerPaquetes(@Path("pilotoId") pilotoId: Int): List<PaqueteResumenDto>

    /** 200 detalle · 404 la guía no existe · 400 formato distinto de GUA-\d{5}. */
    @GET("api/tracking/{guia}")
    suspend fun obtenerDetalle(@Path("guia") guia: String): Response<PaqueteDetalleDto>

    /** 200 detalle actualizado · 400 estado/fecha/formato inválido · 404 guía inexistente. */
    @PUT("api/tracking/{guia}/estado")
    suspend fun actualizarEstado(
        @Path("guia") guia: String,
        @Body body: ActualizarEstadoRequest,
    ): Response<PaqueteDetalleDto>
}
