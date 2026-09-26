package com.trackinglogistic.ui.piloto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trackinglogistic.domain.model.CodigoPiloto
import com.trackinglogistic.domain.repository.PilotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PilotoUiState(
    val codigo: String = "",
    val error: String? = null,
)

@HiltViewModel
class PilotoViewModel @Inject constructor(
    private val pilotoRepository: PilotoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PilotoUiState())
    val uiState: StateFlow<PilotoUiState> = _uiState.asStateFlow()

    fun onCodigoChange(texto: String) {
        _uiState.update { it.copy(codigo = texto.take(8), error = null) }
    }

    /** Al guardar, MainViewModel detecta el piloto en DataStore y muestra la lista. */
    fun onIngresar() {
        val codigo = CodigoPiloto.normalizar(_uiState.value.codigo)
        if (codigo == null) {
            _uiState.update { it.copy(error = "Usa el formato PIL-001") }
            return
        }
        viewModelScope.launch {
            pilotoRepository.guardar(codigo)
            // El ViewModel vive con la Activity: se limpia para que al "Cambiar piloto" el campo esté vacío.
            _uiState.value = PilotoUiState()
        }
    }
}
