package com.trackinglogistic.domain.repository

import com.trackinglogistic.domain.model.EstadoPaquete
import com.trackinglogistic.domain.model.Paquete
import com.trackinglogistic.domain.model.ResultadoBusqueda
import com.trackinglogistic.domain.model.ResultadoRefresco
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first: los Flow siempre leen de la base local (fuente de verdad);
 * la red solo se usa para refrescarla o sincronizar cambios.
 */
interface PaqueteRepository {
    fun observarPaquetes(pilotoId: String): Flow<List<Paquete>>
    fun observarPaquete(guia: String): Flow<Paquete?>
    suspend fun refrescar(pilotoId: String): ResultadoRefresco

    /** Descarga el detalle completo (teléfono, dirección, observaciones) de una guía y lo guarda en Room. */
    suspend fun refrescarDetalle(guia: String): ResultadoRefresco
    suspend fun buscarRemoto(guia: String): ResultadoBusqueda

    /**
     * Guarda el cambio localmente (syncPendiente = true) y agenda su envío al servidor.
     * [observaciones]: null = no modificar las del servidor.
     */
    suspend fun actualizarEstado(guia: String, estado: EstadoPaquete, observaciones: String? = null)

    /** Envía al servidor los cambios pendientes. true si no quedó nada pendiente. */
    suspend fun sincronizarPendientes(): Boolean
}
