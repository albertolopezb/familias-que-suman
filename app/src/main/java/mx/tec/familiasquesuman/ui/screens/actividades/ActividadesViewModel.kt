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
 * Mientras no hay servidor, la lista nunca falla sola.
 *
 * Los estados vacío y de error se revisan en las previews de ActividadesScreen,
 * que es donde deben revisarse. Para verlos además en el emulador, cambia el
 * valor de MODO_ACTUAL aquí abajo. Esta constante desaparece con el backend.
 */
enum class ModoDePrueba { NORMAL, VACIA, ERROR }

private val MODO_ACTUAL = ModoDePrueba.NORMAL

class ActividadesViewModel(
    private val actividadRepository: ActividadRepository
) : ViewModel() {

    private val _estado =
        MutableStateFlow<UiState<List<ActividadConAsociacion>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<ActividadConAsociacion>>> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            // La red tarda. Sin esta pausa el estado Cargando no se alcanza a ver,
            // y es justo uno de los que hay que revisar en esta etapa.
            delay(700)
            _estado.value = when (MODO_ACTUAL) {
                ModoDePrueba.VACIA -> UiState.Exito(emptyList())
                ModoDePrueba.ERROR -> UiState.Error(
                    "Revisa tu conexión: la app necesita internet para mostrar " +
                        "los lugares disponibles al momento."
                )
                ModoDePrueba.NORMAL ->
                    UiState.Exito(actividadRepository.getActividadesConAsociacion())
            }
        }
    }
}
