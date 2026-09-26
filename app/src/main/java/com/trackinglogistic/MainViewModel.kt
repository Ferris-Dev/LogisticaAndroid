package com.trackinglogistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackinglogistic.domain.repository.PilotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface SesionState {
    data object Cargando : SesionState
    data object SinPiloto : SesionState
    data class ConPiloto(val pilotoId: String) : SesionState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    pilotoRepository: PilotoRepository,
) : ViewModel() {

    val sesion: StateFlow<SesionState> = pilotoRepository.pilotoId
        .map { id -> if (id == null) SesionState.SinPiloto else SesionState.ConPiloto(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SesionState.Cargando)
}
