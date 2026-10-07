package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.MapaRepository
import mx.tec.familiasquesuman.domain.MapaCercano
import mx.tec.familiasquesuman.ui.state.UiState

/** Lo que enseña la pestaña "Mapa" de Actividades: qué hay cerca de la familia. */
class MapaViewModel(
    private val mapaRepository: MapaRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<MapaCercano>>(UiState.Cargando)
    val estado: StateFlow<UiState<MapaCercano>> = _estado.asStateFlow()

    /**
     * Se llama cada vez que la pantalla vuelve a mostrarse, para que el mapa
     * refleje lo que se haya creado o borrado. Si ya había punteros, se quedan
     * a la vista mientras llegan los nuevos.
     */
    fun cargar() {
        viewModelScope.launch {
            if (_estado.value !is UiState.Exito) _estado.value = UiState.Cargando
            _estado.value = try {
                UiState.Exito(mapaRepository.getMapaCercano())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UiState.Error("No pudimos cargar lo que hay cerca de ti. Intenta de nuevo.")
            }
        }
    }
}
