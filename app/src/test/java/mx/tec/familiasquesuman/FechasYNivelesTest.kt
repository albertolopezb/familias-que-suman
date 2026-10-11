package mx.tec.familiasquesuman

import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.EstadoParticipacion
import mx.tec.familiasquesuman.domain.Participacion
import mx.tec.familiasquesuman.domain.actividadesPorMes
import mx.tec.familiasquesuman.domain.cuantoFalta
import mx.tec.familiasquesuman.domain.fechaDeTexto
import mx.tec.familiasquesuman.domain.proximasEnOrden
import mx.tec.familiasquesuman.ui.components.iniciales
import mx.tec.familiasquesuman.ui.screens.actividades.componentes.encabezadoInvitacion
import mx.tec.familiasquesuman.ui.screens.perfil.nivelPara
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class FechasYNivelesTest {
    private val hoy = LocalDate.of(2026, 10, 10)

    private fun actividad(id: String, fecha: String, yaPaso: Boolean = false) =
        Actividad(id, id, "a1", fecha, "9:00 – 12:00", "", null, "", 10, 5, yaPaso = yaPaso)

    @Test fun leeLaFechaDelTexto() {
        assertEquals(LocalDate.of(2026, 11, 28), fechaDeTexto("sábado, 28 de noviembre", hoy))
        assertNull(fechaDeTexto("pronto", hoy))
    }

    @Test fun unaFechaMuyAtrasadaEsDelAnoSiguiente() {
        assertEquals(LocalDate.of(2027, 1, 9), fechaDeTexto("sábado, 9 de enero", hoy))
    }

    @Test fun diceCuantoFaltaComoPersona() {
        assertEquals("Hoy", cuantoFalta(hoy, hoy))
        assertEquals("Mañana", cuantoFalta(hoy.plusDays(1), hoy))
        assertEquals("En 5 días", cuantoFalta(hoy.plusDays(5), hoy))
        assertEquals("En 3 semanas", cuantoFalta(hoy.plusDays(21), hoy))
    }

    @Test fun lasProximasVanEnOrdenYSinLasQuePasaron() {
        val lista = listOf(
            actividad("b", "sábado, 28 de noviembre"),
            actividad("a", "domingo, 11 de octubre"),
            actividad("vieja", "sábado, 3 de octubre"),
            actividad("pasada", "sábado, 17 de octubre", yaPaso = true)
        )
        assertEquals(listOf("a", "b"), proximasEnOrden(lista, hoy).map { it.id })
    }

    @Test fun cuentaLasActividadesPorMes() {
        val historial = listOf("SEPTIEMBRE", "SEPTIEMBRE", "AGOSTO", "JULIO").mapIndexed { i, mes ->
            Participacion("p$i", "x", "y", "z", mes, EstadoParticipacion.SIN_PENDIENTES)
        }
        assertEquals(
            listOf("Jun" to 0, "Jul" to 1, "Ago" to 1, "Sep" to 2, "Oct" to 0),
            actividadesPorMes(historial, 5, hoy)
        )
    }

    @Test fun losNivelesSubenConLasActividades() {
        assertEquals("Por estrenar", nivelPara(0).nombre)
        assertEquals("Familia que suma", nivelPara(4).nombre)
        assertEquals(5, nivelPara(4).meta)
        assertEquals("Familia solidaria", nivelPara(5).nombre)
        assertNull(nivelPara(12).siguiente)
    }

    @Test fun lasInicialesIgnoranPalabrasDeRelleno() {
        assertEquals("FR", iniciales("Familia Rodríguez"))
        assertEquals("CD", iniciales("Casa de Día Los Robles"))
        assertEquals("?", iniciales(" "))
    }

    @Test fun laInvitacionDiceQuienVa() {
        assertEquals("LA FAMILIA RODRÍGUEZ VA A", encabezadoInvitacion("Familia Rodríguez"))
        assertEquals("ANA VA A", encabezadoInvitacion("Ana"))
        assertEquals("TE INVITO A", encabezadoInvitacion(null))
    }
}
