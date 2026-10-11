package mx.tec.familiasquesuman.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** De qué trata cada notificación de la bandeja; define su ícono y su color. */
enum class TipoAviso { BIENVENIDA, INSCRIPCION, CANCELACION, RECORDATORIO, FAVORITO, CIERRE, URGENCIA }

/**
 * Una notificación de la bandeja (Mi perfil → Notificaciones). El [id] es estable: el mismo aviso
 * no llega dos veces aunque se vuelva a revisar.
 */
data class Aviso(
    val id: String,
    val tipo: TipoAviso,
    val titulo: String,
    val texto: String,
    /** Cuándo llegó, en milisegundos. */
    val creado: Long = 0,
    val leido: Boolean = false,
    /** Lo que se abre al tocarla: la actividad, la campaña o, sin ninguna, Mi perfil. */
    val actividadId: String? = null,
    val campanaId: String? = null
)

/** Los tres interruptores de Ajustes → Notificaciones. */
data class QueAvisar(val recordatorios: Boolean = true, val favoritos: Boolean = true, val urgencias: Boolean = false)

/** Una campaña favorita avisa que cierra cuando le quedan estos días o menos. */
private const val DIAS_CIERRE = 21

/** Una actividad cercana con estos lugares libres o menos cuenta como urgencia. */
private const val LUGARES_URGENCIA = 3

/**
 * Los avisos que tocan hoy según lo que hace la familia y lo que eligió en Ajustes:
 * - Recordatorio: el día anterior y el mismo día de cada actividad a la que van.
 * - Favoritos: actividades nuevas de las asociaciones que siguen y campañas favoritas que cierran pronto.
 * - Urgencias: actividades de su ciudad en los próximos 7 días a las que les quedan pocos lugares.
 * Quien llama agrega solo los que no tenía; aquí no se sabe qué ya llegó.
 */
fun avisosDelDia(
    hoy: LocalDate,
    actividades: List<Actividad>,
    campanas: List<Campana>,
    asociaciones: Map<String, String>,
    inscritas: Set<String>,
    favoritas: Set<String>,
    lugares: (Actividad) -> Int,
    ciudad: String,
    queAvisar: QueAvisar
): List<Aviso> {
    val proximas = actividades
        .filter { !it.yaPaso }
        .mapNotNull { a -> fechaDeTexto(a.fecha, hoy)?.let { a to ChronoUnit.DAYS.between(hoy, it) } }
        .filter { (_, dias) -> dias >= 0 }
        .sortedBy { it.second }
    val avisos = mutableListOf<Aviso>()

    if (queAvisar.recordatorios) {
        proximas.filter { (a, dias) -> a.id in inscritas && dias <= 1 }.forEach { (a, dias) ->
            avisos += Aviso(
                id = "recordatorio:${a.id}:${a.fecha}:$dias",
                tipo = TipoAviso.RECORDATORIO,
                titulo = if (dias == 0L) "Hoy tienen actividad" else "Mañana tienen actividad",
                texto = "${a.titulo}, a las ${horaDe(a)} en ${lugarDe(a)}.",
                actividadId = a.id
            )
        }
    }

    if (queAvisar.favoritos) {
        proximas
            .filter { (a, _) -> a.asociacionId in favoritas && a.id !in inscritas && a.ciudad == ciudad && lugares(a) > 0 }
            .forEach { (a, dias) ->
                val quien = asociaciones[a.asociacionId] ?: "una asociación que siguen"
                avisos += Aviso(
                    id = "favorito:${a.id}:${a.fecha}",
                    tipo = TipoAviso.FAVORITO,
                    titulo = "Nueva actividad de $quien",
                    texto = "${a.titulo}, ${cuandoEs(a, dias)} a las ${horaDe(a)}. ${quedan(lugares(a))}.",
                    actividadId = a.id
                )
            }
        campanas
            .filter { (it.id in favoritas || it.asociacionId in favoritas) && it.cierra.isNotBlank() }
            .mapNotNull { c -> fechaDeTexto(c.cierra, hoy)?.let { c to ChronoUnit.DAYS.between(hoy, it) } }
            .filter { (_, dias) -> dias in 0..DIAS_CIERRE }
            .forEach { (c, dias) ->
                avisos += Aviso(
                    id = "cierre:${c.id}:${c.cierra}",
                    tipo = TipoAviso.CIERRE,
                    titulo = "${c.titulo} cierra pronto",
                    texto = "Reciben donaciones hasta el ${c.cierra}. " +
                        when (dias) { 0L -> "Hoy es el último día."; 1L -> "Mañana es el último día."; else -> "Faltan $dias días." },
                    campanaId = c.id
                )
            }
    }

    if (queAvisar.urgencias) {
        proximas
            .filter { (a, dias) -> dias <= 7 && a.id !in inscritas && a.ciudad == ciudad && lugares(a) in 1..LUGARES_URGENCIA }
            .forEach { (a, dias) ->
                avisos += Aviso(
                    id = "urgencia:${a.id}:${a.fecha}",
                    tipo = TipoAviso.URGENCIA,
                    titulo = "Faltan voluntarios en $ciudad",
                    texto = "${quedan(lugares(a))} en ${a.titulo}, ${cuandoEs(a, dias)} a las ${horaDe(a)}.",
                    actividadId = a.id
                )
            }
    }
    return avisos
}

/** Al crear la cuenta. */
fun avisoBienvenida() = Aviso(
    id = "bienvenida",
    tipo = TipoAviso.BIENVENIDA,
    titulo = "¡Bienvenidos a Familias que Suman!",
    texto = "Registren a los integrantes de su familia en Mi perfil y quedarán anotados cada vez que se inscriban."
)

/** Al confirmar una inscripción. Si se vuelven a inscribir, reemplaza al anterior. */
fun avisoInscripcion(a: Actividad, personas: Int) = Aviso(
    id = "inscripcion:${a.id}",
    tipo = TipoAviso.INSCRIPCION,
    titulo = "Ya están inscritos",
    texto = "${if (personas == 1) "Va 1 persona" else "Van $personas personas"} a ${a.titulo}, " +
        "el ${a.fecha.replace(",", "")} a las ${horaDe(a)}.",
    actividadId = a.id
)

/** Al cancelar una inscripción. */
fun avisoCancelacion(a: Actividad, liberados: Int) = Aviso(
    id = "cancelacion:${a.id}",
    tipo = TipoAviso.CANCELACION,
    titulo = "Cancelaron su inscripción",
    texto = "Liberaron ${if (liberados == 1) "1 lugar" else "$liberados lugares"} en ${a.titulo}. " +
        "Pueden volver a inscribirse mientras haya lugares.",
    actividadId = a.id
)

/** "Ahora", "Hace 5 min", "Hace 3 h", "Ayer", "Hace 4 días" o "8 oct". */
fun haceCuanto(creado: Long, ahora: Long, zona: ZoneId = ZoneId.systemDefault()): String {
    val minutos = (ahora - creado).coerceAtLeast(0) / 60_000
    val dia = Instant.ofEpochMilli(creado).atZone(zona).toLocalDate()
    val dias = ChronoUnit.DAYS.between(dia, Instant.ofEpochMilli(ahora).atZone(zona).toLocalDate())
    return when {
        minutos < 1 -> "Ahora"
        minutos < 60 -> "Hace $minutos min"
        dias == 0L -> "Hace ${minutos / 60} h"
        dias == 1L -> "Ayer"
        dias < 7 -> "Hace $dias días"
        else -> "${dia.dayOfMonth} ${MESES_AVISO[dia.monthValue - 1]}"
    }
}

private val MESES_AVISO = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

/** "16:00 – 18:00" → "16:00". */
private fun horaDe(a: Actividad) = a.horario.substringBefore("–").substringBefore("-").trim()

private fun lugarDe(a: Actividad) = a.puntoDeEncuentro.ifBlank { a.direccion }

private fun cuandoEs(a: Actividad, dias: Long) = when (dias) {
    0L -> "hoy"
    1L -> "mañana"
    else -> "el ${a.fecha.replace(",", "")}"
}

private fun quedan(n: Int) = if (n == 1) "Queda 1 lugar" else "Quedan $n lugares"
