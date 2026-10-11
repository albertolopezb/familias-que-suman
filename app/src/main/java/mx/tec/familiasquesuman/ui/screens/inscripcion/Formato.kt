package mx.tec.familiasquesuman.ui.screens.inscripcion

import mx.tec.familiasquesuman.domain.Acompanante

// Las fechas todavía son texto ("Sábado 12 de septiembre"). Cuando pasen a LocalDate
// estas funciones se cambian por un DateTimeFormatter y las pantallas no se enteran.

/** "Sábado 12 de septiembre" → "Sáb 12 sep" */
internal fun fechaCorta(fecha: String): String {
    val partes = fecha.split(" ").filter { it.isNotBlank() && it != "de" }
    if (partes.size < 3) return fecha
    return "${partes[0].take(3)} ${partes[1]} ${partes[2].take(3)}"
}

/** "9:00 a 12:00" → "9:00" */
internal fun horaDeInicio(horario: String): String = horario.substringBefore(" a ").trim()

/** [Ana Rodríguez, Mateo Rodríguez, Renata Rodríguez] → "Ana, Mateo y Renata" */
internal fun nombresDePila(personas: List<Acompanante>): String {
    val nombres = personas.map { it.nombre.substringBefore(" ") }
    return when (nombres.size) {
        0 -> ""
        1 -> nombres[0]
        else -> nombres.dropLast(1).joinToString(", ") + " y " + nombres.last()
    }
}

/** Como en P-06: el adulto sin "años" ("38"), los niños con "9 años". */
internal fun edadParaMostrar(persona: Acompanante, esTitular: Boolean): String = when {
    persona.edad <= 0 -> ""
    esTitular -> "${persona.edad}"
    persona.edad == 1 -> "1 año"
    else -> "${persona.edad} años"
}

internal fun lugares(n: Int) = if (n == 1) "1 lugar" else "$n lugares"

private val patronCorreo = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")
internal fun esCorreoValido(correo: String) = patronCorreo.matches(correo.trim())

/**
 * La nota de P-20 con lo que falta de verdad para la actividad: "Faltan 48 días para la
 * actividad, todavía estás a tiempo." Sin fecha legible, una nota general.
 */
internal fun faltaParaLaActividad(fecha: String?, hoy: java.time.LocalDate = java.time.LocalDate.now()): String {
    val dia = fecha?.let { mx.tec.familiasquesuman.domain.fechaDeTexto(it, hoy) }
        ?: return "Puedes cancelar hasta 12 horas antes de la actividad."
    val dias = java.time.temporal.ChronoUnit.DAYS.between(hoy, dia)
    return when {
        dias <= 0 -> "La actividad es hoy: avisa a la asociación si ya no pueden ir."
        dias == 1L -> "La actividad es mañana, todavía estás a tiempo."
        else -> "Faltan $dias días para la actividad, todavía estás a tiempo."
    }
}
