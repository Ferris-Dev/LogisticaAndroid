package com.trackinglogistic.domain.usecase

import com.trackinglogistic.domain.model.Paquete
import com.trackinglogistic.domain.model.ResultadoRefresco
import com.trackinglogistic.domain.repository.PaqueteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObtenerPaquetesUseCase @Inject constructor(
    private val repository: PaqueteRepository,
) {
    operator fun invoke(pilotoId: String): Flow<List<Paquete>> = repository.observarPaquetes(pilotoId)

    fun porGuia(guia: String): Flow<Paquete?> = repository.observarPaquete(guia)

    suspend fun refrescar(pilotoId: String): ResultadoRefresco = repository.refrescar(pilotoId)

    suspend fun refrescarDetalle(guia: String): ResultadoRefresco = repository.refrescarDetalle(guia)
}
