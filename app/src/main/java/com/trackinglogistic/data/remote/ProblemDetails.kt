package com.trackinglogistic.data.remote

import com.trackinglogistic.data.remote.dto.ProblemDetailsDto
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response

private const val ERROR_SERVIDOR = "Error del servidor, intenta de nuevo"

/** Lee el errorBody() como ProblemDetails. Null si no viene o no es JSON válido. */
fun Response<*>.problemDetails(json: Json): ProblemDetailsDto? =
    runCatching { errorBody()?.string()?.let { json.decodeFromString<ProblemDetailsDto>(it) } }.getOrNull()

fun HttpException.problemDetails(json: Json): ProblemDetailsDto? = response()?.problemDetails(json)

/**
 * Mensaje apto para el usuario: el "detail" del servidor en errores 4xx y uno genérico en 5xx.
 * Nunca se muestra el JSON crudo ni el stack trace.
 */
fun mensajeDeError(codigo: Int, problema: ProblemDetailsDto?): String =
    if (codigo >= 500) {
        ERROR_SERVIDOR
    } else {
        problema?.detail?.takeIf { it.isNotBlank() }
            ?: problema?.title?.takeIf { it.isNotBlank() }
            ?: "Error inesperado ($codigo)"
    }

/** true si el 400 se debe a fechaCambio (p. ej. reloj del dispositivo más de 5 min adelantado). */
fun ProblemDetailsDto.errorEnCampo(campo: String): Boolean =
    errors.orEmpty().keys.any { it.equals(campo, ignoreCase = true) }
