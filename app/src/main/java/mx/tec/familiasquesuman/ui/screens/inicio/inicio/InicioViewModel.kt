package mx.tec.familiasquesuman.ui.screens.inicio

import mx.tec.familiasquesuman.ui.state.intentar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.data.PreferenciasStore
import mx.tec.familiasquesuman.domain.Asociacion

class InicioViewModel(
    private val actividadRepository: ActividadRepository,
    private val preferencias: PreferenciasStore
) : ViewModel() {

    /** La ciudad vive en Preferencias: la que se elige al abrir la app, en el Inicio o en Ajustes es la misma. */
    val ciudadElegida: String get() = preferencias.preferencias.value.ciudad
    val prefs = preferencias.preferencias

    private val _asociacionDestacada = MutableStateFlow<Asociacion?>(null)
    val asociacionDestacada: StateFlow<Asociacion?> = _asociacionDestacada

    init {
        cargarDestacados()
    }

    fun cambiarCiudad(nuevaCiudad: String) = preferencias.cambiarCiudad(nuevaCiudad)

    private fun cargarDestacados() {
        viewModelScope.launch {
            intentar { actividadRepository.getAsociaciones() }.onSuccess { _asociacionDestacada.value = it.firstOrNull() }
        }
    }
}
