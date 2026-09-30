package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.PerfilRepository
import mx.tec.familiasquesuman.ui.state.UiState

data class PreferenciasAjustes(
    // Valores iniciales de la maqueta P-35, no preferencias persistidas del usuario.
    val recordatorioActividades: Boolean = true,
    val avisosFavoritos: Boolean = true,
    val urgenciasCiudad: Boolean = false,
    val mensaje: String? = null
)

class AjustesViewModel(private val perfilRepository: PerfilRepository) : ViewModel() {
    private val _ciudad = MutableStateFlow<UiState<String>>(UiState.Cargando)
    val ciudad = _ciudad.asStateFlow()
    private val _preferencias = MutableStateFlow(PreferenciasAjustes())
    val preferencias = _preferencias.asStateFlow()
    private var carga: Job? = null

    init { cargarCiudad() }

    fun reintentar() = cargarCiudad()

    private fun cargarCiudad() {
        carga?.cancel()
        carga = viewModelScope.launch {
            _ciudad.value = UiState.Cargando
            try {
                _ciudad.value = UiState.Exito(perfilRepository.getFamilia().ciudad)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _ciudad.value = UiState.Error("No se pudo cargar tu ciudad. Intenta de nuevo.")
            }
        }
    }

    fun cambiarRecordatorio(activo: Boolean) {
        _preferencias.update { it.copy(recordatorioActividades = activo, mensaje = null) }
    }

    fun cambiarAvisosFavoritos(activo: Boolean) {
        _preferencias.update { it.copy(avisosFavoritos = activo, mensaje = null) }
    }

    fun cambiarUrgenciasCiudad(activo: Boolean) {
        _preferencias.update { it.copy(urgenciasCiudad = activo, mensaje = null) }
    }

    fun solicitarCambioCiudad() {
        // El selector de Inicio no actualiza Familia ni comparte estado con Perfil.
        _preferencias.update { it.copy(mensaje = "El cambio de ciudad de tu perfil todavía no está disponible.") }
    }

    fun solicitarCierreSesion() {
        // No hay contrato de autenticación ni sesión que cerrar.
        _preferencias.update { it.copy(mensaje = "El cierre de sesión todavía no está disponible.") }
    }
}
