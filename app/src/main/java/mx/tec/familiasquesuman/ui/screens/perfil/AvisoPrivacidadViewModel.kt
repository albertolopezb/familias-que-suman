package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EstadoAvisoPrivacidad(val mensaje: String? = null)

class AvisoPrivacidadViewModel : ViewModel() {
    private val _estado = MutableStateFlow(EstadoAvisoPrivacidad())
    val estado = _estado.asStateFlow()

    fun solicitarEliminacion() {
        // Pendiente del equipo: contrato para solicitar eliminación. No se borra ni se envía nada.
        _estado.value = EstadoAvisoPrivacidad(
            "La solicitud de eliminación todavía no está disponible. No se ha enviado ninguna solicitud ni se han eliminado datos."
        )
    }
}
