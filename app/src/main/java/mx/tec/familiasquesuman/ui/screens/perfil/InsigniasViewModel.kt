package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.PerfilRepository
import mx.tec.familiasquesuman.domain.Impacto
import mx.tec.familiasquesuman.ui.state.UiState

enum class TipoInsignia { PRIMERA_VEZ, MANOS_LLENAS, EN_FAMILIA, ANO_COMPLETO, EMBAJADORA }

/** Una insignia y cuánto lleva la familia hacia ella. */
data class EstadoInsignia(
    val tipo: TipoInsignia,
    val titulo: String,
    val descripcion: String,
    val actual: Int,
    val meta: Int
) {
    val obtenida: Boolean get() = actual >= meta
}

/** El nivel sale de las actividades realizadas; [meta] es cuántas pide el siguiente. */
data class Nivel(val nombre: String, val siguiente: String?, val actual: Int, val meta: Int)

data class DatosInsignias(val impacto: Impacto, val nivel: Nivel, val insignias: List<EstadoInsignia>)

/** Los niveles de la familia, de menos a más actividades. */
fun nivelPara(actividades: Int): Nivel = when {
    actividades < 1 -> Nivel("Por estrenar", "Familia que suma", actividades, 1)
    actividades < 5 -> Nivel("Familia que suma", "Familia solidaria", actividades, 5)
    actividades < 10 -> Nivel("Familia solidaria", "Familia que transforma", actividades, 10)
    else -> Nivel("Familia que transforma", null, actividades, actividades)
}

/**
 * Las insignias se calculan con lo que la familia ha hecho: su historial (RF-11), sus
 * acompañantes y las veces que ha invitado a otras familias. Con el backend, el servidor
 * guardará cuándo se ganó cada una.
 */
class InsigniasViewModel(private val perfilRepository: PerfilRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<DatosInsignias>>(UiState.Cargando)
    val estado = _estado.asStateFlow()
    private var carga: Job? = null
    private var ultimo: Triple<String?, Boolean, Int> = Triple(null, false, 0)

    fun cargar(correo: String?, conHijos: Boolean, invitaciones: Int) {
        ultimo = Triple(correo, conHijos, invitaciones)
        carga?.cancel()
        carga = viewModelScope.launch {
            if (_estado.value !is UiState.Exito) _estado.value = UiState.Cargando
            try {
                val impacto = perfilRepository.getImpacto(correo)
                val meses = perfilRepository.getHistorial(correo).map { it.mes }.distinct().size
                _estado.value = UiState.Exito(DatosInsignias(
                    impacto = impacto,
                    nivel = nivelPara(impacto.actividadesRealizadas),
                    insignias = listOf(
                        EstadoInsignia(TipoInsignia.PRIMERA_VEZ, "Primera vez", "Tu primera actividad",
                            impacto.actividadesRealizadas.coerceAtMost(1), 1),
                        EstadoInsignia(TipoInsignia.EN_FAMILIA, "En familia", "Fuiste con tus hijos",
                            if (conHijos && impacto.actividadesRealizadas > 0) 1 else 0, 1),
                        EstadoInsignia(TipoInsignia.EMBAJADORA, "Familia que invita", "Invitaste a otra familia",
                            invitaciones.coerceAtMost(1), 1),
                        EstadoInsignia(TipoInsignia.MANOS_LLENAS, "Manos llenas", "5 campañas apoyadas",
                            impacto.campanasApoyadas.coerceAtMost(5), 5),
                        EstadoInsignia(TipoInsignia.ANO_COMPLETO, "Año completo", "12 meses con actividad",
                            meses.coerceAtMost(12), 12)
                    )
                ))
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _estado.value = UiState.Error("No se pudo cargar el reconocimiento. Intenta de nuevo.")
            }
        }
    }

    fun reintentar() = cargar(ultimo.first, ultimo.second, ultimo.third)
}
