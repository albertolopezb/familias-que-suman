package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.PerfilRepository

/**
 * `folio` es el de la solicitud ya enviada (null = todavía no hay);
 * `confirmando` abre el diálogo de "¿seguro?" y `enviando` apaga el botón mientras se registra.
 */
data class EstadoAvisoPrivacidad(
    val confirmando: Boolean = false,
    val enviando: Boolean = false,
    val folio: String? = null,
    val mensaje: String? = null
)

class AvisoPrivacidadViewModel(private val repositorio: PerfilRepository) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoAvisoPrivacidad())
    val estado = _estado.asStateFlow()

    /** Si esta cuenta ya mandó su solicitud, la pantalla lo muestra desde el principio. */
    fun cargar(correo: String?) {
        if (correo == null) return
        viewModelScope.launch {
            repositorio.getSolicitudEliminacion(correo)?.let { folio -> _estado.update { it.copy(folio = folio) } }
        }
    }

    fun pedirConfirmacion() = _estado.update { it.copy(confirmando = true, mensaje = null) }

    fun cancelarConfirmacion() = _estado.update { it.copy(confirmando = false) }

    fun confirmarEliminacion(correo: String) {
        if (_estado.value.enviando) return
        viewModelScope.launch {
            _estado.update { it.copy(confirmando = false, enviando = true) }
            delay(900)
            val folio = repositorio.solicitarEliminacion(correo)
            _estado.update { it.copy(enviando = false, folio = folio) }
        }
    }
}
