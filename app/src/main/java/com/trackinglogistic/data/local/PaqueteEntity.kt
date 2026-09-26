package com.trackinglogistic.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "paquetes", indices = [Index("pilotoId")])
data class PaqueteEntity(
    @PrimaryKey val guia: String,
    /** Código del piloto (PIL-001). Null para guías obtenidas por búsqueda que no son de su lista. */
    val pilotoId: String?,
    val cliente: String,
    val telefono: String,
    val direccion: String,
    val zona: String,
    val estado: String,
    val observaciones: String? = null,
    /** ISO-8601 UTC, tal como la envía el servidor. */
    val ultimaActualizacion: String? = null,
    /** false mientras solo se tenga el resumen de la lista (sin teléfono ni dirección). */
    val detalleCargado: Boolean = false,
    /** true mientras el cambio de estado local no se haya confirmado con el servidor. */
    val syncPendiente: Boolean = false,
    /** Momento (ISO-8601 UTC) en que el piloto cambió el estado; se envía en el PUT. */
    val fechaCambio: String? = null,
    /** Observaciones escritas junto al cambio pendiente; null = no modificar las del servidor. */
    val observacionesPendientes: String? = null,
    /** Aviso para el piloto cuando el servidor no aplicó su cambio (superado o rechazado). */
    val avisoSync: String? = null,
)
