package mx.tec.familiasquesuman.ui.screens.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.domain.Asociacion

class AsociacionViewModel(
    private val actividadRepository: ActividadRepository
) : ViewModel() {

    private val _asociacion = MutableStateFlow<Asociacion?>(null)
    val asociacion: StateFlow<Asociacion?> = _asociacion

    private val _esFavorito = MutableStateFlow(false)
    val esFavorito: StateFlow<Boolean> = _esFavorito

    fun cargarAsociacion(id: String) {
        viewModelScope.launch {
            _asociacion.value = actividadRepository.getAsociacion(id)
        }
    }

    fun toggleFavorito() {
        _esFavorito.value = !_esFavorito.value
    }
}