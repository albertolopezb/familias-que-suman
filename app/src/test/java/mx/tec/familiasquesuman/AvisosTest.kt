package mx.tec.familiasquesuman

import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.domain.QueAvisar
import mx.tec.familiasquesuman.domain.TipoAviso
import mx.tec.familiasquesuman.domain.avisoInscripcion
import mx.tec.familiasquesuman.domain.avisosDelDia
import mx.tec.familiasquesuman.domain.haceCuanto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class AvisosTest {

    private val hoy = LocalDate.of(2026, 10, 11)

    private fun actividad(id: String, fecha: String, libres: Int = 10, asociacion: String = "a1", ciudad: String = "Monterrey") =
        Actividad(
            id, "Actividad $id", asociacion, fecha, "10:00 – 13:00", "Av. Colón 905",
            edadMinima = null, descripcion = "", cupoTotal = 20, lugaresDisponibles = libres,
            puntoDeEncuentro = "Albergue", ciudad = ciudad
        )

    private val campana = Campana(
        "c3", "Suma a su Mesa", "", "Alimentos", cierra = "30 de octubre", urgente = false,
        descripcion = "", unidadMeta = "", metaTotal = 0, completados = 0, articulos = emptyList<ArticuloMeta>()
    )

    private fun avisos(
        actividades: List<Actividad>,
        inscritas: Set<String> = emptySet(),
        favoritas: Set<String> = emptySet(),
        queAvisar: QueAvisar = QueAvisar(),
        campanas: List<Campana> = emptyList()
    ) = avisosDelDia(
        hoy, actividades, campanas, mapOf("a1" to "Casa de Día Los Robles"), inscritas, favoritas,
        lugares = { it.lugaresDisponibles }, ciudad = "Monterrey", queAvisar = queAvisar
    )

    @Test
    fun recuerda_el_dia_anterior_y_el_mismo_dia_pero_no_antes() {
        val lista = avisos(
            listOf(actividad("hoy", "domingo, 11 de octubre"), actividad("manana", "lunes, 12 de octubre"),
                actividad("luego", "sábado, 17 de octubre")),
            inscritas = setOf("hoy", "manana", "luego")
        ).filter { it.tipo == TipoAviso.RECORDATORIO }
        assertEquals(listOf("Hoy tienen actividad", "Mañana tienen actividad"), lista.map { it.titulo })
    }

    @Test
    fun sin_el_interruptor_de_recordatorios_no_recuerda() {
        val lista = avisos(
            listOf(actividad("manana", "lunes, 12 de octubre")), inscritas = setOf("manana"),
            queAvisar = QueAvisar(recordatorios = false)
        )
        assertTrue(lista.none { it.tipo == TipoAviso.RECORDATORIO })
    }

    @Test
    fun avisa_lo_nuevo_de_una_asociacion_favorita_si_no_estan_inscritos() {
        val lista = avisos(
            listOf(actividad("nueva", "sábado, 17 de octubre"), actividad("ya", "sábado, 17 de octubre"),
                actividad("llena", "sábado, 17 de octubre", libres = 0), actividad("pasada", "sábado, 3 de octubre")),
            inscritas = setOf("ya"), favoritas = setOf("a1")
        ).filter { it.tipo == TipoAviso.FAVORITO }
        assertEquals(listOf("nueva"), lista.map { it.actividadId })
        assertEquals("Nueva actividad de Casa de Día Los Robles", lista.single().titulo)
    }

    @Test
    fun avisa_que_cierra_una_campana_favorita() {
        val lista = avisos(emptyList(), favoritas = setOf("c3"), campanas = listOf(campana))
        assertEquals("Suma a su Mesa cierra pronto", lista.single().titulo)
        assertTrue(lista.single().texto.endsWith("Faltan 19 días."))
    }

    @Test
    fun las_urgencias_solo_si_las_pidieron() {
        val pocas = listOf(actividad("pocas", "martes, 13 de octubre", libres = 2))
        assertTrue(avisos(pocas).none { it.tipo == TipoAviso.URGENCIA })
        val lista = avisos(pocas, queAvisar = QueAvisar(urgencias = true)).filter { it.tipo == TipoAviso.URGENCIA }
        assertTrue(lista.single().texto.startsWith("Quedan 2 lugares"))
    }

    @Test
    fun la_inscripcion_dice_cuantos_van() {
        assertEquals(
            "Van 3 personas a Actividad x, el sábado 17 de octubre a las 10:00.",
            avisoInscripcion(actividad("x", "sábado, 17 de octubre"), 3).texto
        )
    }

    @Test
    fun hace_cuanto_llego() {
        val zona = ZoneId.of("America/Monterrey")
        val ahora = hoy.atTime(18, 0).atZone(zona).toInstant().toEpochMilli()
        assertEquals("Ahora", haceCuanto(ahora - 20_000, ahora, zona))
        assertEquals("Hace 5 min", haceCuanto(ahora - 5 * 60_000, ahora, zona))
        assertEquals("Hace 3 h", haceCuanto(ahora - 3 * 3_600_000, ahora, zona))
        assertEquals("Ayer", haceCuanto(ahora - 20 * 3_600_000, ahora, zona))
        assertEquals("8 sep", haceCuanto(hoy.minusDays(33).atTime(9, 0).atZone(zona).toInstant().toEpochMilli(), ahora, zona))
    }
}
