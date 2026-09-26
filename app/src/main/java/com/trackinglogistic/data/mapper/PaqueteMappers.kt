package com.trackinglogistic.data.mapper

import com.trackinglogistic.data.local.PaqueteEntity
import com.trackinglogistic.data.remote.dto.PaqueteDetalleDto
import com.trackinglogistic.data.remote.dto.PaqueteResumenDto
import com.trackinglogistic.domain.model.EstadoPaquete
import com.trackinglogistic.domain.model.Paquete

// DTO (red) → Entity (Room) → Domain (UI). La UI nunca ve DTOs ni entidades.

fun PaqueteResumenDto.toEntity(pilotoId: String): PaqueteEntity = PaqueteEntity(
    guia = guia,
    pilotoId = pilotoId,
    cliente = cliente,
    telefono = "",
    direccion = "",
    zona = zona,
    estado = EstadoPaquete.desdeApi(estado).name,
    detalleCargado = false,
)

/** [pilotoId] null: el piloto se conserva del registro local si ya existía (ver PaqueteDao.guardarDetalle). */
fun PaqueteDetalleDto.toEntity(pilotoId: String? = null): PaqueteEntity = PaqueteEntity(
    guia = guia,
    pilotoId = pilotoId,
    cliente = cliente,
    telefono = telefono.orEmpty(),
    direccion = direccion,
    zona = zona,
    estado = EstadoPaquete.desdeApi(estado).name,
    observaciones = observaciones?.takeIf { it.isNotBlank() },
    ultimaActualizacion = ultimaActualizacion,
    detalleCargado = true,
)

fun PaqueteEntity.toDomain(): Paquete = Paquete(
    guia = guia,
    cliente = cliente,
    telefono = telefono,
    direccion = direccion,
    zona = zona,
    estado = EstadoPaquete.desdeApi(estado),
    syncPendiente = syncPendiente,
    observaciones = observaciones?.takeIf { it.isNotBlank() },
    detalleCargado = detalleCargado,
    ultimaActualizacion = ultimaActualizacion,
    avisoSync = avisoSync,
)
