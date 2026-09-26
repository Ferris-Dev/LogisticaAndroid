package com.trackinglogistic.domain.usecase

import com.trackinglogistic.domain.model.Guia
import com.trackinglogistic.domain.model.Paquete
import com.trackinglogistic.domain.model.ResultadoBusqueda
import com.trackinglogistic.domain.repository.PaqueteRepository
import javax.inject.Inject

/**
 * Búsqueda por guía en dos pasos: primero filtra la lista local; si no hay
 * coincidencias, la pantalla invoca la consulta remota (GET /api/tracking/{guia}).
 */
class BuscarPorGuiaUseCase @Inject constructor(
    private val repository: PaqueteRepository,
) {
    fun filtrarLocal(paquetes: List<Paquete>, consulta: String): List<Paquete> =
        Guia.filtrar(paquetes, consulta)

    suspend fun buscarRemoto(consulta: String): ResultadoBusqueda {
        val guia = Guia.normalizar(consulta) ?: return ResultadoBusqueda.FormatoInvalido()
        return repository.buscarRemoto(guia)
    }
}
