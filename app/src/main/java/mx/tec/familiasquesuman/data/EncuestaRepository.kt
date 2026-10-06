package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.MomentoEncuesta
import mx.tec.familiasquesuman.domain.RespuestaEncuesta
import mx.tec.familiasquesuman.domain.RespuestaPregunta

/**
 * Las respuestas de las encuestas de antes y después (RF-13), ligadas a la actividad.
 * Viven en memoria; con el backend serán un POST y un GET por actividad o por familia.
 */
class EncuestaRepository {

    private val respuestas = mutableListOf(
        RespuestaEncuesta(
            id = "e1", actividadId = "p3", actividadTitulo = "Regalando Estrellas Visita al Materno Infantil",
            correo = "ana.rodriguez@correo.com", familia = "Familia Rodríguez", momento = MomentoEncuesta.DESPUES,
            respuestas = listOf(
                RespuestaPregunta(1, "¿Cómo se sintieron en la actividad?", "Muy bien, volveríamos"),
                RespuestaPregunta(2, "¿Qué aprendieron tus hijos en esta actividad?", "Entendieron mejor cómo viven otras familias"),
                RespuestaPregunta(3, "¿Qué tan clara fue la organización del día?", "Muy clara"),
                RespuestaPregunta(4, "¿Volverían a participar en otra actividad?", "Sí, pronto")
            ),
            fecha = "2 ago 2026"
        )
    )

    suspend fun getTodas(): List<RespuestaEncuesta> = respuestas.toList()

    suspend fun getDeFamilia(correo: String): List<RespuestaEncuesta> =
        respuestas.filter { it.correo.equals(correo, ignoreCase = true) }

    suspend fun getPorActividad(actividadId: String): List<RespuestaEncuesta> =
        respuestas.filter { it.actividadId == actividadId }

    /** Guarda las respuestas; si la misma familia ya había contestado esa encuesta, se reemplazan. */
    suspend fun guardar(nueva: RespuestaEncuesta) {
        respuestas.removeAll {
            it.correo.equals(nueva.correo, ignoreCase = true) &&
                it.actividadId == nueva.actividadId && it.momento == nueva.momento
        }
        respuestas.add(0, nueva)
    }
}
