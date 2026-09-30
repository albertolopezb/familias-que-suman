package mx.tec.familiasquesuman.notificaciones

import android.content.Context

/**
 * Los dos avisos del Figma (P-25), para el botón de demo de Ajustes (parte 5).
 * Cada función devuelve false si no se pudo enviar porque falta el permiso.
 *
 * En la etapa 2 el recordatorio se programa con WorkManager 24 horas antes, y la
 * urgencia llega de Firebase Messaging; los textos y Notificaciones.enviar se quedan igual.
 */
object NotificacionesDemo {

    fun dispararRecordatorio(context: Context): Boolean = Notificaciones.enviar(
        context,
        titulo = "Mañana tienen actividad",
        texto = "Preparar despensas de fin de mes, sábado a las 9:00 en el Comedor San Bernabé. " +
            "Toca para ver los detalles.",
        actividadId = "act1",
        canal = Notificaciones.CANAL_RECORDATORIOS
    )

    fun dispararUrgencia(context: Context): Boolean = Notificaciones.enviar(
        context,
        titulo = "Se necesitan 5 voluntarios urgentes",
        texto = "Mañana en el Albergue Nuevo Amanecer, en Monterrey. Coincide con las causas que sigues.",
        actividadId = "act3",
        canal = Notificaciones.CANAL_URGENCIAS
    )

    /**
     * "Ya están inscritos". La parte 3 la manda sola al confirmar una inscripción;
     * sin argumentos sirve de demo con los datos del Figma.
     * @param cuando "el sábado 12 de septiembre a las 9:00"
     */
    fun dispararInscripcionConfirmada(
        context: Context,
        tituloActividad: String = "Preparar despensas de fin de mes",
        cuando: String = "el sábado 12 de septiembre a las 9:00",
        personas: Int = 3,
        actividadId: String = "act1"
    ): Boolean = Notificaciones.enviar(
        context,
        titulo = "Ya están inscritos",
        texto = "Van ${if (personas == 1) "1 persona" else "$personas personas"} a $tituloActividad, $cuando. " +
            "Toca para ver los detalles.",
        actividadId = actividadId,
        canal = Notificaciones.CANAL_INSCRIPCIONES
    )
}
