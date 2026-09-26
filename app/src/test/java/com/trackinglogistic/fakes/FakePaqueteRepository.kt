package com.trackinglogistic.fakes

import com.trackinglogistic.domain.model.EstadoPaquete
import com.trackinglogistic.domain.model.Paquete
import com.trackinglogistic.domain.model.ResultadoBusqueda
import com.trackinglogistic.domain.model.ResultadoRefresco
import com.trackinglogistic.domain.repository.PaqueteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Repositorio en memoria que imita el comportamiento offline-first de la implementación real. */
class FakePaqueteRepository(
    iniciales: List<Paquete> = emptyList(),
) : PaqueteRepository {

    private val paquetes = MutableStateFlow(iniciales.associateBy { it.guia })

    var resultadoRefresco: ResultadoRefresco = ResultadoRefresco.Exito
    var remotos: Map<String, Paquete> = emptyMap()
    var fallarBusquedaSinConexion = false
    var fallarActualizacion = false

    val guiasBuscadasRemoto = mutableListOf<String>()
    val detallesRefrescados = mutableListOf<String>()
    val actualizaciones = mutableListOf<Pair<String, EstadoPaquete>>()
    val observacionesEnviadas = mutableListOf<String?>()
    var sincronizacionesProgramadas = 0
        private set

    override fun observarPaquetes(pilotoId: String): Flow<List<Paquete>> =
        paquetes.map { it.values.sortedBy(Paquete::guia) }

    override fun observarPaquete(guia: String): Flow<Paquete?> = paquetes.map { it[guia] }

    override suspend fun refrescar(pilotoId: String): ResultadoRefresco = resultadoRefresco

    override suspend fun refrescarDetalle(guia: String): ResultadoRefresco {
        detallesRefrescados += guia
        return resultadoRefresco
    }

    override suspend fun buscarRemoto(guia: String): ResultadoBusqueda {
        guiasBuscadasRemoto += guia
        if (fallarBusquedaSinConexion) return ResultadoBusqueda.SinConexion
        return remotos[guia]?.let(ResultadoBusqueda::Encontrado)
            ?: ResultadoBusqueda.NoExiste("La guía $guia no existe")
    }

    override suspend fun actualizarEstado(guia: String, estado: EstadoPaquete, observaciones: String?) {
        if (fallarActualizacion) error("Fallo simulado de base de datos")
        actualizaciones += guia to estado
        observacionesEnviadas += observaciones
        paquetes.value = paquetes.value.toMutableMap().apply {
            computeIfPresent(guia) { _, p -> p.copy(estado = estado, syncPendiente = true) }
        }
        sincronizacionesProgramadas++
    }

    /** Simula la respuesta del PUT con un estado vigente distinto (cambio superado). */
    fun imponerEstadoDelServidor(guia: String, estado: EstadoPaquete) {
        paquetes.value = paquetes.value.toMutableMap().apply {
            computeIfPresent(guia) { _, p -> p.copy(estado = estado, syncPendiente = false) }
        }
    }

    override suspend fun sincronizarPendientes(): Boolean {
        paquetes.value = paquetes.value.mapValues { it.value.copy(syncPendiente = false) }
        return true
    }
}
