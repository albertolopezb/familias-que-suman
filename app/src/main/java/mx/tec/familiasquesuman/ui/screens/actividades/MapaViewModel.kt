package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.MapaRepository
import mx.tec.familiasquesuman.domain.MapaDeCiudad
import mx.tec.familiasquesuman.ui.state.UiState

/** Lo que enseña el mapa del Inicio: los punteros de la ciudad elegida. */
class MapaViewModel(
    private val mapaRepository: MapaRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<MapaDeCiudad>>(UiState.Cargando)
    val estado: StateFlow<UiState<MapaDeCiudad>> = _estado.asStateFlow()

    /**
     * Se llama cada vez que el Inicio vuelve a mostrarse o cambia la ciudad, para que el
     * mapa refleje lo que se haya creado o borrado. Si ya había punteros de esa misma
     * ciudad, se quedan a la vista mientras llegan los nuevos.
     */
    fun cargar(ciudad: String) {
        viewModelScope.launch {
            val actual = _estado.value
            if (actual !is UiState.Exito || actual.datos.ciudad != ciudad) _estado.value = UiState.Cargando
            _estado.value = try {
                UiState.Exito(mapaRepository.getMapaDeCiudad(ciudad))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UiState.Error("No pudimos cargar el mapa. Intenta de nuevo.")
            }
        }
    }
}
