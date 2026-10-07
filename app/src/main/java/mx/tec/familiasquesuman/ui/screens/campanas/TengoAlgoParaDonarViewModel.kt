package mx.tec.familiasquesuman.ui.screens.campanas

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mx.tec.familiasquesuman.ui.state.UiState

data class TengoAlgoParaDonarUiState(
    val datos: UiState<List<CentroRecepcion>> = UiState.Cargando,
    val tipoSeleccionado: String = TODOS,
    val centroExpandido: String? = null
) {
    // Derivado: no se almacena una segunda lista ni un contador mutable.
    val centrosVisibles: List<CentroRecepcion>
        get() {
            val centros = (datos as? UiState.Exito)?.datos.orEmpty()
            return if (tipoSeleccionado == TODOS) centros
            else centros.filter { tipoSeleccionado in it.tipos }
        }

    companion object {
        const val TODOS = "Todos"
    }
}

/** Fuente local única; el proveedor permite comprobar errores sin agregar un backend. */
class TengoAlgoParaDonarViewModel(
    private val obtenerCentros: () -> List<CentroRecepcion> = { CentrosDeRecepcion }
) : ViewModel() {
    private val _estado = MutableStateFlow(TengoAlgoParaDonarUiState())
    val estado = _estado.asStateFlow()

    init { cargar() }

    fun cargar() {
        _estado.update { it.copy(datos = UiState.Cargando) }
        val resultado = try {
            UiState.Exito(obtenerCentros())
        } catch (e: Exception) {
            UiState.Error("No se pudieron cargar los centros de donación.")
        }
        _estado.update { it.copy(datos = resultado) }
    }

    fun seleccionarTipo(tipo: String) {
        if (tipo == TengoAlgoParaDonarUiState.TODOS || tipo in TiposDeDonacion) {
            _estado.update { it.copy(tipoSeleccionado = tipo) }
        }
    }

    fun seleccionarCentro(id: String) {
        if (_estado.value.centrosVisibles.none { it.id == id }) return
        _estado.update { it.copy(centroExpandido = if (it.centroExpandido == id) null else id) }
    }

    // Estados simulables para la entrega; no se usan en la carga normal.
    internal fun simularCarga() { _estado.update { it.copy(datos = UiState.Cargando) } }
    internal fun simularError() {
        _estado.update { it.copy(datos = UiState.Error("No se pudieron cargar los centros de donación.")) }
    }
}
