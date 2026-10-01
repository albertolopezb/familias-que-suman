package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
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
    private val perfilRepository: PerfilRepository,
    private val actividadRepository: ActividadRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<MisActividades>>(UiState.Cargando)
    val estado: StateFlow<UiState<MisActividades>> = _estado.asStateFlow()

    /** Las actividades a las que la cuenta está inscrita ahora mismo. */
    private var inscritas: Set<String> = emptySet()
    private var primeraCarga = true

    fun cargar(ids: Set<String> = inscritas) {
        inscritas = ids
        viewModelScope.launch {
            if (primeraCarga) {
                _estado.value = UiState.Cargando
                delay(500)
                primeraCarga = false
            }
            _estado.value = UiState.Exito(
                MisActividades(
                    proximas = ids.mapNotNull { id ->
                        runCatching { actividadRepository.getActividadConAsociacion(id) }.getOrNull()
                    },
                    historial = perfilRepository.getHistorial()
                )
            )
        }
    }
}
