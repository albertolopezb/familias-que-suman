package mx.tec.familiasquesuman.ui.screens.actividades

import mx.tec.familiasquesuman.domain.Coordenada
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.data.red.ApiExcepcion
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.ui.state.UiState

/**
 * Para ver los estados vacío y de error sin apagar el servidor queda el botón oculto: mantén
 * presionado el título "Actividades". Cada toque largo pasa al siguiente modo
 * (normal → vacía → error → normal).
 */
enum class ModoDePrueba { NORMAL, VACIA, ERROR }

class ActividadesViewModel(
    private val actividadRepository: ActividadRepository
) : ViewModel() {

    private val _estado =
        MutableStateFlow<UiState<List<ActividadConAsociacion>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<ActividadConAsociacion>>> = _estado.asStateFlow()

    private var modo = ModoDePrueba.NORMAL

    /** Lo último que sí cargó. Es lo que enseña la pantalla sin conexión. */
    private val _guardadas = MutableStateFlow<List<ActividadConAsociacion>>(emptyList())
    val guardadas: StateFlow<List<ActividadConAsociacion>> = _guardadas.asStateFlow()

    /** Lo que respondió el servidor si una acción de administración no se pudo (o null). */
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            if (_estado.value !is UiState.Exito) _estado.value = UiState.Cargando
            _estado.value = when (modo) {
                ModoDePrueba.VACIA -> UiState.Exito(emptyList())
                ModoDePrueba.ERROR -> UiState.Error(SIN_CONEXION)
                ModoDePrueba.NORMAL -> try {
                    val actividades = actividadRepository.getActividadesConAsociacion()
                    _guardadas.value = actividades
                    UiState.Exito(actividades)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    UiState.Error(if (e is ApiExcepcion && !e.sinConexion) e.message else SIN_CONEXION)
                }
            }
        }
    }

    /** El botón oculto de la lista: pasa al siguiente modo y vuelve a cargar. */
    fun siguienteModoDePrueba() {
        modo = ModoDePrueba.entries[(modo.ordinal + 1) % ModoDePrueba.entries.size]
        cargar()
    }

    fun mensajeVisto() {
        _mensaje.value = null
    }

    /** Las acciones de admin: el servidor revisa el permiso; si dice que no, se avisa y no cambia nada. */
    private fun admin(accion: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensaje.value = (e as? ApiExcepcion)?.message ?: "No se pudo guardar. Intenta de nuevo."
            }
            cargar()
        }
    }

    fun borrarActividad(id: String) = admin { actividadRepository.borrarActividad(id) }

    fun crearActividad(
        titulo: String,
        descripcion: String,
        fecha: String,
        horario: String,
        direccion: String,
        cupoTotal: Int,
        lugaresDisponibles: Int,
        edadMinima: Int?,
        ciudad: String = "Monterrey",
        coordenada: Coordenada? = null
    ) = admin {
        actividadRepository.guardarActividad(
            id = null, titulo = titulo, descripcion = descripcion, fecha = fecha, horario = horario,
            direccion = direccion, cupoTotal = cupoTotal, lugaresDisponibles = lugaresDisponibles,
            edadMinima = edadMinima, ciudad = ciudad, asociacionId = null, coordenada = coordenada
        )
    }

    fun duplicarActividad(id: String) = admin { actividadRepository.duplicarActividad(id) }

    fun editarActividad(
        id: String,
        nuevoTitulo: String,
        nuevaDescripcion: String,
        nuevaFecha: String,
        nuevoHorario: String,
        nuevaDireccion: String,
        nuevoCupoTotal: Int,
        nuevosLugaresDisponibles: Int,
        nuevaEdadMinima: Int?,
        nuevaCoordenada: Coordenada?
    ) = admin {
        val actual = actividadRepository.enMemoria(id)
        actividadRepository.guardarActividad(
            id = id, titulo = nuevoTitulo, descripcion = nuevaDescripcion, fecha = nuevaFecha, horario = nuevoHorario,
            direccion = nuevaDireccion, cupoTotal = nuevoCupoTotal, lugaresDisponibles = nuevosLugaresDisponibles,
            edadMinima = nuevaEdadMinima, ciudad = actual?.actividad?.ciudad ?: "Monterrey", asociacionId = actual?.asociacion?.id,
            coordenada = nuevaCoordenada
        )
    }

    private companion object {
        const val SIN_CONEXION = "Revisa tu conexión: la app necesita internet para mostrar los lugares disponibles al momento."
    }
}
