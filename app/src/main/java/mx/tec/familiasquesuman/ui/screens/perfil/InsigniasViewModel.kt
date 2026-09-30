package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.domain.Impacto
import mx.tec.familiasquesuman.data.PerfilRepository
import mx.tec.familiasquesuman.ui.state.UiState

enum class TipoInsignia { PRIMERA_VEZ, MANOS_LLENAS, EN_FAMILIA, ANO_COMPLETO }

/** Referencias visuales de P-19, no un catálogo ni reconocimientos obtenidos del dominio. */
data class ReferenciaInsignia(val tipo: TipoInsignia, val titulo: String, val descripcion: String)

data class DatosInsignias(val impacto: Impacto, val referencias: List<ReferenciaInsignia>)

class InsigniasViewModel(private val perfilRepository: PerfilRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<DatosInsignias>>(UiState.Cargando)
    val estado = _estado.asStateFlow()
    private var carga: Job? = null

    init { cargar() }

    fun reintentar() = cargar()

    private fun cargar() {
        carga?.cancel()
        carga = viewModelScope.launch {
            _estado.value = UiState.Cargando
            try {
                // Pendiente del equipo: catálogo, reglas de nivel y estado de cada insignia.
                // Ni el total de actividades ni el de campañas certifican una insignia.
                _estado.value = UiState.Exito(DatosInsignias(
                    perfilRepository.getImpacto(),
                    listOf(
                        ReferenciaInsignia(TipoInsignia.PRIMERA_VEZ, "Primera vez", "Tu primera actividad"),
                        ReferenciaInsignia(TipoInsignia.MANOS_LLENAS, "Manos llenas", "5 campañas apoyadas"),
                        ReferenciaInsignia(TipoInsignia.EN_FAMILIA, "En familia", "Fuiste con tus hijos"),
                        ReferenciaInsignia(TipoInsignia.ANO_COMPLETO, "Año completo", "12 meses seguidos")
                    )
                ))
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _estado.value = UiState.Error("No se pudo cargar el reconocimiento. Intenta de nuevo.")
            }
        }
    }
}
