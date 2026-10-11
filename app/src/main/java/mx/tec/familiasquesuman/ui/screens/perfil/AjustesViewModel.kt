package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import mx.tec.familiasquesuman.data.PreferenciasStore
import mx.tec.familiasquesuman.ui.state.UiState

data class PreferenciasAjustes(
    val recordatorioActividades: Boolean = true,
    val avisosFavoritos: Boolean = true,
    val urgenciasCiudad: Boolean = false,
    val mensaje: String? = null
)

/** Ajustes: la ciudad y los avisos, guardados en el teléfono y compartidos con el resto de la app. */
class AjustesViewModel(private val store: PreferenciasStore) : ViewModel() {

    val ciudad: StateFlow<UiState<String>> = store.preferencias
        .map { UiState.Exito(it.ciudad) as UiState<String> }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Exito(store.preferencias.value.ciudad))

    val preferencias: StateFlow<PreferenciasAjustes> = store.preferencias
        .map { PreferenciasAjustes(it.recordatorioActividades, it.avisosFavoritos, it.urgenciasCiudad) }
        .stateIn(
            viewModelScope, SharingStarted.Eagerly,
            store.preferencias.value.let { PreferenciasAjustes(it.recordatorioActividades, it.avisosFavoritos, it.urgenciasCiudad) }
        )

    fun reintentar() = Unit

    fun cambiarCiudad(ciudad: String) = store.cambiarCiudad(ciudad)
    fun cambiarRecordatorio(activo: Boolean) = store.cambiarRecordatorio(activo)
    fun cambiarAvisosFavoritos(activo: Boolean) = store.cambiarAvisosFavoritos(activo)
    fun cambiarUrgenciasCiudad(activo: Boolean) = store.cambiarUrgenciasCiudad(activo)
}
