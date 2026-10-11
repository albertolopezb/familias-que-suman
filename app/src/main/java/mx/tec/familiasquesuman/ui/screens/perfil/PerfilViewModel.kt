package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.PerfilRepository
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Familia
import mx.tec.familiasquesuman.domain.Impacto
import mx.tec.familiasquesuman.domain.actividadesPorMes
import mx.tec.familiasquesuman.ui.state.UiState

data class DatosPerfil(
    val familia: Familia,
    val impacto: Impacto,
    val proximas: List<Actividad>,
    /** Actividades por mes de los últimos cinco meses, sacadas del historial. */
    val porMes: List<Pair<String, Int>> = emptyList()
)

class PerfilViewModel(private val perfilRepository: PerfilRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<DatosPerfil>>(UiState.Cargando)
    val estado = _estado.asStateFlow()
    private var carga: Job? = null
    private var correo: String? = null

    /** Carga el perfil de la cuenta con sesión: su impacto y su historial son solo suyos. */
    fun cargar(correoDeLaCuenta: String?) {
        correo = correoDeLaCuenta
        carga?.cancel()
        carga = viewModelScope.launch {
            if (_estado.value !is UiState.Exito) _estado.value = UiState.Cargando
            try {
                _estado.value = UiState.Exito(
                    DatosPerfil(
                        familia = perfilRepository.getFamilia(),
                        impacto = perfilRepository.getImpacto(correoDeLaCuenta),
                        proximas = emptyList(),
                        porMes = actividadesPorMes(perfilRepository.getHistorial(correoDeLaCuenta))
                    )
                )
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _estado.value = UiState.Error("No se pudo cargar tu perfil. Intenta de nuevo.")
            }
        }
    }

    fun reintentar() = cargar(correo)
}
