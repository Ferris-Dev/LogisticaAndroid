package com.trackinglogistic.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PaqueteDao {

    @Query("SELECT * FROM paquetes WHERE pilotoId = :pilotoId ORDER BY guia")
    fun observarPorPiloto(pilotoId: String): Flow<List<PaqueteEntity>>

    @Query("SELECT * FROM paquetes WHERE guia = :guia")
    fun observar(guia: String): Flow<PaqueteEntity?>

    @Query("SELECT * FROM paquetes WHERE guia = :guia")
    suspend fun obtener(guia: String): PaqueteEntity?

    /** Cola offline en orden cronológico, como exige el last-write-wins del servidor. */
    @Query("SELECT * FROM paquetes WHERE syncPendiente = 1 ORDER BY fechaCambio")
    suspend fun pendientes(): List<PaqueteEntity>

    @Query("SELECT guia FROM paquetes WHERE pilotoId = :pilotoId AND detalleCargado = 0")
    suspend fun guiasSinDetalle(pilotoId: String): List<String>

    @Upsert
    suspend fun upsert(paquetes: List<PaqueteEntity>)

    /** Cambio local inmediato (offline-first). Limpia cualquier aviso anterior. */
    @Query(
        """UPDATE paquetes SET estado = :estado, syncPendiente = 1, fechaCambio = :fechaCambio,
           observacionesPendientes = :observaciones, observaciones = COALESCE(:observaciones, observaciones),
           avisoSync = NULL
           WHERE guia = :guia""",
    )
    suspend fun actualizarEstadoLocal(guia: String, estado: String, fechaCambio: String, observaciones: String?)

    /**
     * Saca el cambio de la cola solo si no hubo otro cambio local mientras la petición estaba
     * en vuelo (la fecha identifica cada cambio, incluso A → B → A). Devuelve las filas afectadas:
     * 0 significa que hay un cambio más nuevo esperando su turno.
     */
    @Query(
        """UPDATE paquetes SET syncPendiente = 0, observacionesPendientes = NULL
           WHERE guia = :guia AND fechaCambio IS :fechaLocal""",
    )
    suspend fun resolverSync(guia: String, fechaLocal: String?): Int

    @Query("UPDATE paquetes SET avisoSync = :aviso WHERE guia = :guia")
    suspend fun guardarAviso(guia: String, aviso: String?)

    @Query("DELETE FROM paquetes WHERE pilotoId = :pilotoId AND syncPendiente = 0 AND guia NOT IN (:vigentes)")
    suspend fun eliminarObsoletos(pilotoId: String, vigentes: List<String>)

    /**
     * Aplica la lista (resúmenes) del servidor. El resumen no trae teléfono ni dirección,
     * así que se conservan los que ya estaban descargados, y un estado cambiado localmente
     * que aún no se envía no se pisa.
     */
    @Transaction
    suspend fun reemplazarDelPiloto(pilotoId: String, resumenes: List<PaqueteEntity>) {
        val combinados = resumenes.map { remoto ->
            val local = obtener(remoto.guia) ?: return@map remoto
            local.copy(
                pilotoId = pilotoId,
                cliente = remoto.cliente,
                zona = remoto.zona,
                estado = if (local.syncPendiente) local.estado else remoto.estado,
            )
        }
        upsert(combinados)
        eliminarObsoletos(pilotoId, resumenes.map { it.guia })
    }

    /**
     * Guarda el detalle del servidor (GET o respuesta del PUT) conservando el piloto asignado,
     * el aviso y, si hay un cambio local pendiente, el estado y observaciones de ese cambio.
     */
    @Transaction
    suspend fun guardarDetalle(detalle: PaqueteEntity) {
        val local = obtener(detalle.guia)
        val combinado = when {
            local == null -> detalle
            local.syncPendiente -> detalle.copy(
                pilotoId = local.pilotoId,
                estado = local.estado,
                observaciones = if (local.observacionesPendientes != null) local.observaciones else detalle.observaciones,
                syncPendiente = true,
                fechaCambio = local.fechaCambio,
                observacionesPendientes = local.observacionesPendientes,
                avisoSync = local.avisoSync,
            )
            else -> detalle.copy(pilotoId = local.pilotoId, avisoSync = local.avisoSync)
        }
        upsert(listOf(combinado))
    }
}
