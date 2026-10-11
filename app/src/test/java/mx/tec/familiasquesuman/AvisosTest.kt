package mx.tec.familiasquesuman

import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.domain.Aviso
import mx.tec.familiasquesuman.domain.QueAvisar
import mx.tec.familiasquesuman.domain.TipoAviso
import mx.tec.familiasquesuman.domain.avisoInscripcion
import mx.tec.familiasquesuman.domain.avisosDelDia
import mx.tec.familiasquesuman.domain.haceCuanto
import mx.tec.familiasquesuman.domain.horaDeInicio
import mx.tec.familiasquesuman.domain.juntarAvisos
import mx.tec.familiasquesuman.domain.porNotificar
import mx.tec.familiasquesuman.domain.yaTermino
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
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
        campanas: List<Campana> = emptyList(),
        ahora: java.time.LocalDateTime = hoy.atTime(8, 0)
    ) = avisosDelDia(
        ahora, actividades, campanas, mapOf("a1" to "Casa de Día Los Robles"), inscritas, favoritas,
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

    @Test
    fun lo_de_hoy_que_ya_empezo_no_se_recuerda_ni_se_ofrece() {
        val deHoy = listOf(actividad("hoy", "domingo, 11 de octubre", libres = 2))
        val tarde = hoy.atTime(10, 30)
        assertTrue(avisos(deHoy, inscritas = setOf("hoy"), ahora = tarde).isEmpty())
        assertTrue(avisos(deHoy, favoritas = setOf("a1"), queAvisar = QueAvisar(urgencias = true), ahora = tarde).isEmpty())
        assertEquals(1, avisos(deHoy, inscritas = setOf("hoy"), ahora = hoy.atTime(9, 59)).size)
    }

    @Test
    fun una_actividad_termina_a_su_hora_de_fin() {
        val a = actividad("x", "domingo, 11 de octubre")
        assertEquals(LocalTime.of(9, 0), horaDeInicio("9:00 – 12:00"))
        assertFalse(yaTermino(a, hoy.atTime(12, 59)))
        assertTrue(yaTermino(a, hoy.atTime(13, 0)))
        assertTrue(yaTermino(a, hoy.plusDays(1).atTime(8, 0)))
        assertFalse(yaTermino(a, hoy.minusDays(1).atTime(20, 0)))
    }

    @Test
    fun al_juntar_no_se_repiten_salvo_al_reemplazar() {
        val viejo = Aviso("r1", TipoAviso.RECORDATORIO, "t", "x", creado = 100, leido = true)
        val (sinCambio, nada) = juntarAvisos(listOf(viejo), listOf(viejo.copy(creado = 0, leido = false)), false, 500)
        assertEquals(listOf(viejo), sinCambio)
        assertTrue(nada.isEmpty())
        val (bandeja, llegaron) = juntarAvisos(listOf(viejo), listOf(viejo.copy(texto = "nuevo")), true, 500)
        assertEquals(1, bandeja.size)
        assertEquals("nuevo", bandeja.single().texto)
        assertFalse(llegaron.single().leido)
    }

    @Test
    fun al_telefono_solo_de_dia_sin_leer_y_una_vez() {
        val zona = ZoneId.of("America/Monterrey")
        val nueve = hoy.atTime(9, 0)
        val reciente = nueve.minusHours(1).atZone(zona).toInstant().toEpochMilli()
        val viejo = nueve.minusDays(3).atZone(zona).toInstant().toEpochMilli()
        val bandeja = listOf(
            Aviso("ok", TipoAviso.RECORDATORIO, "t", "x", creado = reciente),
            Aviso("leido", TipoAviso.FAVORITO, "t", "x", creado = reciente, leido = true),
            Aviso("ya", TipoAviso.CIERRE, "t", "x", creado = reciente),
            Aviso("viejo", TipoAviso.URGENCIA, "t", "x", creado = viejo),
            Aviso("inscripcion", TipoAviso.INSCRIPCION, "t", "x", creado = reciente)
        )
        assertEquals(listOf("ok"), porNotificar(bandeja, setOf("ya"), nueve, zona).map { it.id })
        assertTrue(porNotificar(bandeja, emptySet(), hoy.atTime(7, 59), zona).isEmpty())
        assertTrue(porNotificar(bandeja, emptySet(), hoy.atTime(21, 0), zona).isEmpty())
    }
}
