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

// RF-07. Las fechas son texto ("sábado, 26 de septiembre") y no traen año: se toma el
// año en curso, o el siguiente si esa fecha ya quedó muy atrás.

private val meses = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

private fun parsearFecha(fecha: String, hoy: LocalDate = LocalDate.now()): LocalDate? {
    val texto = fecha.lowercase()
    val dia = Regex("""\b(\d{1,2})\b""").find(texto)?.groupValues?.get(1)?.toIntOrNull() ?: return null
    val mes = meses.indexOfFirst { texto.contains(it) }.takeIf { it >= 0 }?.plus(1) ?: return null
    val candidata = runCatching { LocalDate.of(hoy.year, mes, dia) }.getOrNull() ?: return null
    return if (candidata.isBefore(hoy.minusMonths(2))) candidata.plusYears(1) else candidata
}

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

        val dia = parsearFecha(actividad.fecha)
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
