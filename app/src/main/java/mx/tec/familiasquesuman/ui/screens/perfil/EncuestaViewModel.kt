package mx.tec.familiasquesuman.ui.screens.perfil

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

// Único contenido respaldado por P-22. Pendientes del equipo: preguntas 1, 3 y 4.
val EncuestaFinalP22 = DefinicionEncuesta(4, listOf(PreguntaEncuesta(
    numero = 2,
    texto = "¿Qué aprendieron tus hijos en esta actividad?",
    opciones = listOf(
        "Entendieron mejor cómo viven otras familias",
        "Aprendieron a trabajar en equipo",
        "Se divirtieron, pero no hablamos del tema",
        "Todavía no lo comentamos"
    )
)))

data class EstadoEncuesta(
    val definicion: DefinicionEncuesta,
    val indiceActual: Int = 0,
    val respuestas: Map<Int, Int> = emptyMap(),
    val mensaje: String? = null
) {
    val preguntaActual: PreguntaEncuesta get() = definicion.preguntas[indiceActual]
    val respuestaSeleccionada: Int? get() = respuestas[preguntaActual.numero]
    val puedeAvanzar: Boolean get() = respuestaSeleccionada in preguntaActual.opciones.indices
    val progreso: Float get() = preguntaActual.numero.toFloat() / definicion.totalPreguntas
}

class EncuestaViewModel(definicion: DefinicionEncuesta = EncuestaFinalP22) : ViewModel() {
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

    fun indicarPrivacidadPendiente() {
        _estado.value = _estado.value.copy(mensaje = "El aviso de privacidad todavía no está disponible.")
    }
}
