package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.PerfilRepository
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.Participacion
import mx.tec.familiasquesuman.ui.state.UiState

/** Lo que la pestaña "Mis Actividades" necesita junto: lo que viene y lo que ya pasó. */
data class MisActividades(
    val proximas: List<ActividadConAsociacion>,
    val historial: List<Participacion>
)

class MisActividadesViewModel(
    private val perfilRepository: PerfilRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<MisActividades>>(UiState.Cargando)
    val estado: StateFlow<UiState<MisActividades>> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            delay(500)
            _estado.value = UiState.Exito(
                MisActividades(
                    proximas = perfilRepository.getProximasConAsociacion(),
                    historial = perfilRepository.getHistorial()
                )
            )
        }
    }
}
