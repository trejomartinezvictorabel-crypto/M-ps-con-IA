package mx.campnav.offline.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class TransportMode(val label: String, val speedKmh: Double) {
    CAR("Auto", 45.0),
    MOTORCYCLE("Moto", 40.0),
    BICYCLE("Bici", 15.0),
    TRANSIT("Público", 22.0),
    WALK("A pie", 4.5)
}

data class NavigationUiState(
    val mode: TransportMode = TransportMode.CAR,
    val status: String = "Sin ruta activa. Importa una red vial offline para navegar.",
    val remainingMeters: Double = 0.0,
    val etaMinutes: Int = 0,
    val nextInstruction: String? = null
)

class NavigationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NavigationUiState())
    val uiState: StateFlow<NavigationUiState> = _uiState

    fun setTransportMode(mode: TransportMode) {
        _uiState.value = _uiState.value.copy(
            mode = mode,
            status = "Modo ${mode.label} seleccionado. La ruta se calculará con reglas del modo al cargar la red."
        )
    }

    fun updateProgress(remainingMeters: Double) {
        val state = _uiState.value
        val metersPerMinute = state.mode.speedKmh * 1000.0 / 60.0
        val eta = if (metersPerMinute > 0) (remainingMeters / metersPerMinute).toInt() else 0
        _uiState.value = state.copy(remainingMeters = remainingMeters, etaMinutes = eta)
    }
}
