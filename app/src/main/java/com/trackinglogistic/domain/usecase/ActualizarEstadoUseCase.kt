package com.trackinglogistic.domain.usecase

import com.trackinglogistic.domain.model.EstadoPaquete
import com.trackinglogistic.domain.repository.PaqueteRepository
import javax.inject.Inject

class ActualizarEstadoUseCase @Inject constructor(
    private val repository: PaqueteRepository,
) {
    /** [observaciones] en blanco se tratan como "no modificar"; el API admite hasta 500 caracteres. */
    suspend operator fun invoke(guia: String, estado: EstadoPaquete, observaciones: String? = null) =
        repository.actualizarEstado(
            guia = guia,
            estado = estado,
            observaciones = observaciones?.trim()?.takeIf { it.isNotEmpty() }?.take(MAX_OBSERVACIONES),
        )

    companion object {
        const val MAX_OBSERVACIONES = 500
    }
}
