package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.data.EncuestaRepository
import mx.tec.familiasquesuman.data.PerfilRepository
import mx.tec.familiasquesuman.data.TestimonioRepository
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.EstadoParticipacion
import mx.tec.familiasquesuman.domain.EstadoTestimonio
import mx.tec.familiasquesuman.domain.MomentoEncuesta
import mx.tec.familiasquesuman.domain.Participacion
import mx.tec.familiasquesuman.ui.state.UiState

/** Lo que la pestaña "Mis Actividades" necesita junto: lo que viene y lo que ya pasó. */
data class MisActividades(
    val proximas: List<ActividadConAsociacion>,
    val historial: List<Participacion>,
    /** Actividades próximas cuya encuesta de antes ya se contestó (RF-13). */
    val conEncuestaPrevia: Set<String> = emptySet(),
    /** Actividades (o participaciones) de las que la cuenta ya tiene respuestas que consultar. */
    val conRespuestas: Set<String> = emptySet()
)

class MisActividadesViewModel(
    private val perfilRepository: PerfilRepository,
    private val actividadRepository: ActividadRepository,
    private val testimonioRepository: TestimonioRepository,
    private val encuestaRepository: EncuestaRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<MisActividades>>(UiState.Cargando)
    val estado: StateFlow<UiState<MisActividades>> = _estado.asStateFlow()

    /** Las actividades a las que la cuenta está inscrita ahora mismo. */
    private var inscritas: Set<String> = emptySet()
    private var correo: String? = null
    private var primeraCarga = true

    fun cargar(ids: Set<String> = inscritas, correoDeLaCuenta: String? = correo) {
        inscritas = ids
        correo = correoDeLaCuenta
        viewModelScope.launch {
            if (primeraCarga) {
                _estado.value = UiState.Cargando
                delay(500)
                primeraCarga = false
            }
            val testimonios = correoDeLaCuenta?.let { testimonioRepository.getDeFamilia(it) }.orEmpty()
            val respuestas = correoDeLaCuenta?.let { encuestaRepository.getDeFamilia(it) }.orEmpty()
            val despues = respuestas.filter { it.momento == MomentoEncuesta.DESPUES }.map { it.actividadId }.toSet()
            _estado.value = UiState.Exito(
                MisActividades(
                    proximas = ids.mapNotNull { id ->
                        runCatching { actividadRepository.getActividadConAsociacion(id) }.getOrNull()
                    },
                    historial = perfilRepository.getHistorial().map { p ->
                        p.copy(estado = estadoDe(p, testimonios.firstOrNull { it.participacionId == p.id }?.estado, p.id in despues))
                    },
                    conEncuestaPrevia = respuestas.filter { it.momento == MomentoEncuesta.ANTES }.map { it.actividadId }.toSet(),
                    conRespuestas = respuestas.map { it.actividadId }.toSet()
                )
            )
        }
    }

    /**
     * El pendiente de una participación: si ya mandó testimonio, lo que dijo la revisión; si no,
     * la encuesta de después (hasta que se conteste) y luego el aviso de "compartir testimonio".
     */
    private fun estadoDe(p: Participacion, testimonio: EstadoTestimonio?, encuestaContestada: Boolean) = when (testimonio) {
        EstadoTestimonio.EN_REVISION -> EstadoParticipacion.TESTIMONIO_EN_REVISION
        EstadoTestimonio.AJUSTAR -> EstadoParticipacion.TESTIMONIO_POR_AJUSTAR
        EstadoTestimonio.APROBADO -> EstadoParticipacion.TESTIMONIO_PUBLICADO
        EstadoTestimonio.DESCARTADO -> EstadoParticipacion.TESTIMONIO_DESCARTADO
        null -> if (p.estado == EstadoParticipacion.ENCUESTA_PENDIENTE && !encuestaContestada)
            EstadoParticipacion.ENCUESTA_PENDIENTE else EstadoParticipacion.SIN_PENDIENTES
    }
}
