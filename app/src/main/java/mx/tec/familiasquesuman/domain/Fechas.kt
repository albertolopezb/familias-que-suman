package mx.tec.familiasquesuman.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

// Las fechas de las actividades son texto ("sábado, 26 de septiembre") y no traen año: se toma
// el año en curso, o el siguiente si esa fecha ya quedó muy atrás. Cuando lleguen como LocalDate
// desde el backend, esto desaparece y nadie más cambia.

private val MESES = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

/** "sábado, 28 de noviembre" → 2026-11-28. null si el texto no trae día y mes. */
fun fechaDeTexto(fecha: String, hoy: LocalDate = LocalDate.now()): LocalDate? {
    val candidata = fechaDelAnio(fecha, hoy.year) ?: return null
    return if (candidata.isBefore(hoy.minusMonths(2))) candidata.plusYears(1) else candidata
}

/** Para algo que ya pasó: la fecha más reciente que no sea posterior a hoy. */
fun fechaPasadaDeTexto(fecha: String, hoy: LocalDate = LocalDate.now()): LocalDate? {
    val candidata = fechaDelAnio(fecha, hoy.year) ?: return null
    return if (candidata.isAfter(hoy)) candidata.minusYears(1) else candidata
}

private fun fechaDelAnio(fecha: String, anio: Int): LocalDate? {
    val texto = fecha.lowercase()
    val dia = Regex("""\b(\d{1,2})\b""").find(texto)?.groupValues?.get(1)?.toIntOrNull() ?: return null
    val mes = MESES.indexOfFirst { texto.contains(it) }.takeIf { it >= 0 }?.plus(1) ?: return null
    return runCatching { LocalDate.of(anio, mes, dia) }.getOrNull()
}

/** "9:00 – 12:00" → 9:00. null si el horario no trae horas. */
fun horaDeInicio(horario: String): LocalTime? = horasDe(horario).firstOrNull()

/** "9:00 – 12:00" → 12:00. null si el horario no trae hora de fin. */
fun horaDeFin(horario: String): LocalTime? = horasDe(horario).getOrNull(1)

private fun horasDe(horario: String): List<LocalTime> =
    Regex("""(\d{1,2}):(\d{2})""").findAll(horario).mapNotNull { m ->
        runCatching { LocalTime.of(m.groupValues[1].toInt(), m.groupValues[2].toInt()) }.getOrNull()
    }.toList()

/**
 * true si la actividad ya terminó: fue otro día, o es hoy y ya pasó su hora de fin (o la de
 * inicio, si no trae fin). Sin fecha legible no se puede saber y cuenta como pendiente.
 */
fun yaTermino(actividad: Actividad, ahora: LocalDateTime = LocalDateTime.now()): Boolean =
    yaPasoLaHora(actividad, ahora, horaDeFin(actividad.horario) ?: horaDeInicio(actividad.horario))

/** true si la actividad ya empezó (o terminó): ya no tiene caso recordarla ni invitar a ella. */
fun yaEmpezo(actividad: Actividad, ahora: LocalDateTime = LocalDateTime.now()): Boolean =
    yaPasoLaHora(actividad, ahora, horaDeInicio(actividad.horario))

private fun yaPasoLaHora(actividad: Actividad, ahora: LocalDateTime, hora: LocalTime?): Boolean {
    val hoy = ahora.toLocalDate()
    val dia = fechaDeTexto(actividad.fecha, hoy) ?: return false
    return when {
        dia.isBefore(hoy) -> true
        dia.isAfter(hoy) -> false
        else -> hora != null && !ahora.toLocalTime().isBefore(hora)
    }
}

/** Cuánto falta, dicho como lo diría una persona: "Hoy", "Mañana", "En 5 días", "En 3 semanas". */
fun cuantoFalta(fecha: LocalDate, hoy: LocalDate = LocalDate.now()): String {
    val dias = ChronoUnit.DAYS.between(hoy, fecha)
    return when {
        dias < 0 -> "Ya pasó"
        dias == 0L -> "Hoy"
        dias == 1L -> "Mañana"
        dias < 14 -> "En $dias días"
        dias < 60 -> "En ${dias / 7} semanas"
        else -> "En ${dias / 30} meses"
    }
}

/** Las actividades a las que va la familia, de la más cercana a la más lejana; sin las que ya pasaron. */
fun proximasEnOrden(actividades: List<Actividad>, hoy: LocalDate = LocalDate.now()): List<Actividad> =
    actividades
        .filter { !it.yaPaso }
        .map { it to (fechaDeTexto(it.fecha, hoy) ?: LocalDate.MAX) }
        .filter { (_, dia) -> !dia.isBefore(hoy) }
        .sortedBy { it.second }
        .map { it.first }

private val MESES_CORTOS = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

/**
 * Cuántas actividades hizo la familia en cada uno de los últimos [meses] meses, contando el actual.
 * Sale del historial (RF-11), que trae el mes en mayúsculas ("SEPTIEMBRE").
 */
fun actividadesPorMes(historial: List<Participacion>, meses: Int = 5, hoy: LocalDate = LocalDate.now()): List<Pair<String, Int>> =
    (meses - 1 downTo 0).map { atras ->
        val mes = hoy.minusMonths(atras.toLong()).monthValue
        MESES_CORTOS[mes - 1] to historial.count { it.mes.equals(MESES[mes - 1], ignoreCase = true) }
    }
