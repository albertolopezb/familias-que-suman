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

class DetalleActividadViewModel(
    private val actividadRepository: ActividadRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<ActividadConAsociacion>>(UiState.Cargando)
    val estado: StateFlow<UiState<ActividadConAsociacion>> = _estado.asStateFlow()

    private var idCargado: String? = null

    /** Se llama desde un LaunchedEffect, no desde el cuerpo del composable. */
    fun cargar(id: String) {
        if (idCargado == id && _estado.value !is UiState.Error) return
        idCargado = id
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            delay(450)
            _estado.value = try {
                UiState.Exito(actividadRepository.getActividadConAsociacion(id))
            } catch (e: NoSuchElementException) {
                UiState.Error("Esta actividad ya no existe. Vuelve a la lista.")
            }
        }
    }

    fun reintentar() {
        val id = idCargado ?: return
        idCargado = null
        cargar(id)
    }
}
