package com.trackinglogistic.domain.repository

import kotlinx.coroutines.flow.Flow

interface PilotoRepository {
    /** Código del piloto guardado (ej. "PIL-001"), o null si aún no se ha ingresado. */
    val pilotoId: Flow<String?>
    suspend fun guardar(pilotoId: String)
    suspend fun cerrarSesion()
}
