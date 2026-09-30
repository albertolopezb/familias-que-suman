package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.ui.state.UiState

/**
 * Mientras no hay servidor, la lista nunca falla sola. Estos tres modos
 * permiten llegar a los estados vacío y de error para poder verlos y revisarlos.
 *
 * Se borran cuando entre el backend: ahí la red los produce de verdad.
 */
enum class ModoDePrueba { NORMAL, VACIA, ERROR }

class ActividadesViewModel(
    private val actividadRepository: ActividadRepository
) : ViewModel() {

    private val _estado =
        MutableStateFlow<UiState<List<ActividadConAsociacion>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<ActividadConAsociacion>>> = _estado.asStateFlow()

    private val _modo = MutableStateFlow(ModoDePrueba.NORMAL)
    val modo: StateFlow<ModoDePrueba> = _modo.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            // La red tarda. Sin esta pausa el estado Cargando no se alcanza a ver,
            // y es justo el que hay que revisar en esta etapa.
            delay(700)
            _estado.value = when (_modo.value) {
                ModoDePrueba.VACIA -> UiState.Exito(emptyList())
                ModoDePrueba.ERROR -> UiState.Error(
                    "No se pudieron cargar las actividades. Revisa tu conexión: " +
                        "la app necesita internet para mostrar los lugares disponibles al momento."
                )
                ModoDePrueba.NORMAL ->
                    UiState.Exito(actividadRepository.getActividadesConAsociacion())
            }
        }
    }

    fun cambiarModo(nuevo: ModoDePrueba) {
        _modo.value = nuevo
        cargar()
    }
}
