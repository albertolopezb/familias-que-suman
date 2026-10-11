package mx.tec.familiasquesuman.ui.screens.inscripcion

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.fechaDeTexto

// RF-07. La fecha de la actividad se lee con fechaDeTexto (domain/Fechas.kt).

private fun parsearHoras(horario: String): Pair<LocalTime, LocalTime?>? {
    val horas = Regex("""(\d{1,2}):(\d{2})""").findAll(horario)
        .map { LocalTime.of(it.groupValues[1].toInt() % 24, it.groupValues[2].toInt()) }
        .toList()
    if (horas.isEmpty()) return null
    return horas[0] to horas.getOrNull(1)
}

/** Abre el calendario del teléfono con la actividad ya llena; la persona solo guarda. */
fun agregarActividadAlCalendario(context: Context, actividad: Actividad, asociacion: String) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, actividad.titulo)
        putExtra(CalendarContract.Events.EVENT_LOCATION, actividad.direccion)
        putExtra(CalendarContract.Events.DESCRIPTION, "Con $asociacion. ${actividad.descripcion}")

        val dia = fechaDeTexto(actividad.fecha)
        val horas = parsearHoras(actividad.horario)
        if (dia != null && horas != null) {
            val zona = ZoneId.systemDefault()
            val inicio = LocalDateTime.of(dia, horas.first)
            val fin = LocalDateTime.of(dia, horas.second ?: horas.first.plusHours(2))
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicio.atZone(zona).toInstant().toEpochMilli())
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fin.atZone(zona).toInstant().toEpochMilli())
        } else if (dia != null) {
            val ms = dia.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, ms)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, ms)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        }
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "No encontramos una app de calendario", Toast.LENGTH_SHORT).show()
    }
}
