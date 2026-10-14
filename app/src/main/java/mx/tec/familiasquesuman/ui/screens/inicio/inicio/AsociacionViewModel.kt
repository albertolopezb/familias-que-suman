package mx.tec.familiasquesuman.ui.screens.inicio

import mx.tec.familiasquesuman.ui.state.intentar
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

    fun cargarAsociacion(id: String) {
        viewModelScope.launch {
            intentar { actividadRepository.getAsociacion(id) }.onSuccess { _asociacion.value = it }
        }
    }
}