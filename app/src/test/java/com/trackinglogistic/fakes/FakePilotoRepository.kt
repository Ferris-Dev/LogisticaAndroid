package com.trackinglogistic.fakes

import com.trackinglogistic.domain.repository.PilotoRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakePilotoRepository(inicial: String? = "PIL-001") : PilotoRepository {
    override val pilotoId = MutableStateFlow(inicial)

    override suspend fun guardar(pilotoId: String) {
        this.pilotoId.value = pilotoId
    }

    override suspend fun cerrarSesion() {
        pilotoId.value = null
    }
}
