package mx.tec.familiasquesuman.ui.screens.inicio.inicio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.domain.Asociacion

class InicioViewModel(
    private val actividadRepository: ActividadRepository
) : ViewModel() {

    var ciudadElegida by mutableStateOf("Monterrey")
        private set

    private val _asociacionDestacada = MutableStateFlow<Asociacion?>(null)
    val asociacionDestacada: StateFlow<Asociacion?> = _asociacionDestacada

    init {
        cargarDestacados()
    }

    fun cambiarCiudad(nuevaCiudad: String) {
        ciudadElegida = nuevaCiudad
    }

    private fun cargarDestacados() {
        viewModelScope.launch {
            val lista = actividadRepository.getAsociaciones()
            _asociacionDestacada.value = lista.firstOrNull()
        }
    }
}