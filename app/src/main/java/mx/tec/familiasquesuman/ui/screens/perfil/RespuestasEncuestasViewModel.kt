package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.EncuestaRepository
import mx.tec.familiasquesuman.domain.RespuestaEncuesta

/** La consulta de respuestas de encuestas (RF-13): las de una familia o, sin [correo], las de todas. */
class RespuestasEncuestasViewModel(private val repositorio: EncuestaRepository) : ViewModel() {

    private val _respuestas = MutableStateFlow<List<RespuestaEncuesta>>(emptyList())
    val respuestas: StateFlow<List<RespuestaEncuesta>> = _respuestas.asStateFlow()

    /** @param actividadId si viene, solo las de esa actividad. */
    fun cargar(correo: String?, actividadId: String?) {
        viewModelScope.launch {
            val todas = if (correo != null) repositorio.getDeFamilia(correo) else repositorio.getTodas()
            _respuestas.value = if (actividadId.isNullOrBlank()) todas else todas.filter { it.actividadId == actividadId }
        }
    }
}
