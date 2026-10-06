package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.tec.familiasquesuman.data.EncuestaRepository
import mx.tec.familiasquesuman.domain.MomentoEncuesta
import mx.tec.familiasquesuman.domain.RespuestaEncuesta
import mx.tec.familiasquesuman.domain.RespuestaPregunta
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Contratos de presentación reutilizables; no reemplazan modelos de domain ni un repositorio. */
data class PreguntaEncuesta(val numero: Int, val texto: String, val opciones: List<String>)

data class DefinicionEncuesta(val totalPreguntas: Int, val preguntas: List<PreguntaEncuesta>) {
    init {
        require(totalPreguntas > 0 && preguntas.isNotEmpty())
        require(preguntas.all { it.numero in 1..totalPreguntas && it.opciones.isNotEmpty() })
        require(preguntas.map { it.numero }.distinct().size == preguntas.size)
        require(preguntas.zipWithNext().all { (a, b) -> a.numero < b.numero })
    }
}

data class EstadoEncuesta(
    val definicion: DefinicionEncuesta,
    val indiceActual: Int = 0,
    val respuestas: Map<Int, Int> = emptyMap(),
    val mensaje: String? = null,
    val guardando: Boolean = false
) {
    val preguntaActual: PreguntaEncuesta get() = definicion.preguntas[indiceActual]
    val respuestaSeleccionada: Int? get() = respuestas[preguntaActual.numero]
    val puedeAvanzar: Boolean get() = respuestaSeleccionada in preguntaActual.opciones.indices
    val progreso: Float get() = preguntaActual.numero.toFloat() / definicion.totalPreguntas
    val esUltima: Boolean get() = indiceActual == definicion.preguntas.lastIndex
}

/**
 * Una encuesta contestada de una pregunta a la vez (RF-13). [momento] dice si es la de antes
 * o la de después; al terminar, las respuestas se guardan ligadas a la actividad.
 */
class EncuestaViewModel(
    definicion: DefinicionEncuesta,
    private val momento: MomentoEncuesta,
    private val repositorio: EncuestaRepository
) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoEncuesta(definicion))
    val estado = _estado.asStateFlow()

    fun seleccionarRespuesta(indice: Int) {
        val actual = _estado.value
        if (indice !in actual.preguntaActual.opciones.indices) return
        _estado.value = actual.copy(
            respuestas = actual.respuestas + (actual.preguntaActual.numero to indice), mensaje = null
        )
    }

    /** true únicamente si están disponibles y contestadas todas las preguntas. */
    fun avanzar(): Boolean {
        val actual = _estado.value
        if (!actual.puedeAvanzar) {
            _estado.value = actual.copy(mensaje = "Selecciona una respuesta para continuar.")
            return false
        }
        val siguiente = actual.definicion.preguntas.getOrNull(actual.indiceActual + 1)
        if (siguiente?.numero == actual.preguntaActual.numero + 1) {
            _estado.value = actual.copy(indiceActual = actual.indiceActual + 1, mensaje = null)
            return false
        }
        val completa = (1..actual.definicion.totalPreguntas).all { numero ->
            val pregunta = actual.definicion.preguntas.firstOrNull { it.numero == numero }
            pregunta != null && actual.respuestas[numero] in pregunta.opciones.indices
        }
        if (!completa) {
            _estado.value = actual.copy(mensaje = "Las preguntas restantes todavía no están disponibles. Tu respuesta se conserva en esta pantalla.")
        }
        return completa
    }

    /** Regresa a la pregunta anterior; en la primera no hace nada y la pantalla cierra. */
    fun retroceder(): Boolean {
        val actual = _estado.value
        if (actual.indiceActual == 0) return false
        _estado.value = actual.copy(indiceActual = actual.indiceActual - 1, mensaje = null)
        return true
    }

    /**
     * Guarda lo contestado ligado a la actividad y avisa con [alTerminar]. Solo guarda si
     * todas las preguntas están contestadas, y una sola vez aunque se toque el botón varias.
     */
    fun guardar(
        actividadId: String,
        actividadTitulo: String,
        correo: String,
        familia: String,
        alTerminar: () -> Unit
    ) {
        val actual = _estado.value
        if (actual.guardando) return
        val contestadas = actual.definicion.preguntas.mapNotNull { pregunta ->
            actual.respuestas[pregunta.numero]?.takeIf { it in pregunta.opciones.indices }?.let {
                RespuestaPregunta(pregunta.numero, pregunta.texto, pregunta.opciones[it])
            }
        }
        if (contestadas.size != actual.definicion.preguntas.size) return
        _estado.value = actual.copy(guardando = true)
        viewModelScope.launch {
            repositorio.guardar(
                RespuestaEncuesta(
                    id = "e${System.currentTimeMillis()}",
                    actividadId = actividadId,
                    actividadTitulo = actividadTitulo,
                    correo = correo,
                    familia = familia,
                    momento = momento,
                    respuestas = contestadas,
                    fecha = LocalDate.now().format(FormatoFecha)
                )
            )
            _estado.value = _estado.value.copy(guardando = false)
            alTerminar()
        }
    }

    private companion object {
        val FormatoFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "MX"))
    }
}
