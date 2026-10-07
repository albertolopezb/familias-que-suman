package mx.tec.familiasquesuman.ui.screens.actividades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.ActividadRepository
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.ui.state.UiState

/**
 * Mientras no hay servidor, la lista nunca falla sola. Para ver los estados
 * vacío y de error en el emulador hay dos caminos:
 *
 * - El botón oculto: mantén presionado el título "Actividades". Cada toque largo
 *   pasa al siguiente modo (normal → vacía → error → normal).
 * - Cambiar MODO_INICIAL aquí abajo, para que la app ya arranque así.
 *
 * Las dos cosas desaparecen con el backend.
 */
enum class ModoDePrueba { NORMAL, VACIA, ERROR }

private val MODO_INICIAL = ModoDePrueba.NORMAL

class ActividadesViewModel(
    private val actividadRepository: ActividadRepository
) : ViewModel() {

    private val _estado =
        MutableStateFlow<UiState<List<ActividadConAsociacion>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<ActividadConAsociacion>>> = _estado.asStateFlow()

    private var modo = MODO_INICIAL

    /** Lo último que sí cargó. Es lo que enseña la pantalla sin conexión. */
    private val _guardadas = MutableStateFlow<List<ActividadConAsociacion>>(emptyList())
    val guardadas: StateFlow<List<ActividadConAsociacion>> = _guardadas.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            // La red tarda. Sin esta pausa el estado Cargando no se alcanza a ver,
            // y es justo uno de los que hay que revisar en esta etapa.
            delay(700)
            _estado.value = when (modo) {
                ModoDePrueba.VACIA -> UiState.Exito(emptyList())
                ModoDePrueba.ERROR -> UiState.Error(
                    "Revisa tu conexión: la app necesita internet para mostrar " +
                        "los lugares disponibles al momento."
                )
                ModoDePrueba.NORMAL -> {
                    val actividades = actividadRepository.getActividadesConAsociacion()
                    _guardadas.value = actividades
                    UiState.Exito(actividades)
                }
            }
        }
    }

    /** El botón oculto de la lista: pasa al siguiente modo y vuelve a cargar. */
    fun siguienteModoDePrueba() {
        modo = ModoDePrueba.entries[(modo.ordinal + 1) % ModoDePrueba.entries.size]
        cargar()
    }

    fun borrarActividad(id: String) {
        viewModelScope.launch {
            val estadoActual = _estado.value
            if (estadoActual is UiState.Exito) {
                // Filtramos la lista eliminando la actividad con el ID correspondiente
                val listaActualizada = estadoActual.datos.filterNot { it.actividad.id == id }

                // Actualizamos el estado de la vista al instante
                _estado.value = UiState.Exito(listaActualizada)
                _guardadas.value = listaActualizada
            }
        }
    }

    fun crearActividad(
        titulo: String,
        descripcion: String,
        fecha: String,
        horario: String,
        direccion: String,
        cupoTotal: Int,
        lugaresDisponibles: Int,
        edadMinima: Int?
    ) {
        viewModelScope.launch {
            val estadoActual = _estado.value
            val nuevaActividad = mx.tec.familiasquesuman.domain.Actividad(
                id = System.currentTimeMillis().toString(),
                asociacionId = "1",
                titulo = titulo,
                descripcion = descripcion,
                fecha = fecha,
                horario = horario,
                direccion = direccion,
                cupoTotal = cupoTotal,
                lugaresDisponibles = lugaresDisponibles,
                edadMinima = edadMinima
            )

            // Si ya hay una lista cargada, creamos una Asociación simulada y la añadimos a la lista
            if (estadoActual is UiState.Exito) {
                val nuevaConAsociacion = mx.tec.familiasquesuman.domain.ActividadConAsociacion(
                    actividad = nuevaActividad,
                    asociacion = estadoActual.datos.firstOrNull()?.asociacion
                        ?: mx.tec.familiasquesuman.domain.Asociacion(
                            id = "1",
                            nombre = "Asociación del Norte",
                            categoria = "Comunidad",
                            descripcion = "",
                            direccion = "Monterrey, N.L.",
                            telefono = "",
                            whatsapp = "",
                            correo = ""
                        )
                )

                val listaActualizada = estadoActual.datos + nuevaConAsociacion
                _estado.value = UiState.Exito(listaActualizada)
                _guardadas.value = listaActualizada
            }
        }
    }

    fun duplicarActividad(id: String) {
        viewModelScope.launch {
            val estadoActual = _estado.value
            if (estadoActual is UiState.Exito) {
                val elementoOriginal = estadoActual.datos.find { it.actividad.id == id }

                elementoOriginal?.let { item ->
                    val actividadOriginal = item.actividad
                    val actividadDuplicada = actividadOriginal.copy(
                        id = System.currentTimeMillis().toString(), // Genera un ID único basado en el timestamp
                        titulo = "${actividadOriginal.titulo} (Copia)",
                        lugaresDisponibles = actividadOriginal.cupoTotal // Reinicia los lugares disponibles al cupo total
                    )

                    val nuevoItem = item.copy(actividad = actividadDuplicada)
                    val listaActualizada = estadoActual.datos + nuevoItem

                    _estado.value = UiState.Exito(listaActualizada)
                    _guardadas.value = listaActualizada
                }
            }
        }
    }

    fun editarActividad(
        id: String,
        nuevoTitulo: String,
        nuevaDescripcion: String,
        nuevaFecha: String,
        nuevoHorario: String,
        nuevaDireccion: String,
        nuevoCupoTotal: Int,
        nuevosLugaresDisponibles: Int,
        nuevaEdadMinima: Int?
    ) {
        viewModelScope.launch {
            val estadoActual = _estado.value
            if (estadoActual is UiState.Exito) {
                val listaActualizada = estadoActual.datos.map { item ->
                    if (item.actividad.id == id) {
                        val actividadEditada = item.actividad.copy(
                            titulo = nuevoTitulo,
                            descripcion = nuevaDescripcion,
                            fecha = nuevaFecha,
                            horario = nuevoHorario,
                            direccion = nuevaDireccion,
                            cupoTotal = nuevoCupoTotal,
                            lugaresDisponibles = nuevosLugaresDisponibles,
                            edadMinima = nuevaEdadMinima
                        )
                        item.copy(actividad = actividadEditada)
                    } else {
                        item
                    }
                }

                _estado.value = UiState.Exito(listaActualizada)
                _guardadas.value = listaActualizada
            }
        }
    }
}
