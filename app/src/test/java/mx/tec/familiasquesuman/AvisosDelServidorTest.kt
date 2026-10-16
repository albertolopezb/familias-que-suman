package mx.tec.familiasquesuman

import mx.tec.familiasquesuman.domain.AvisoDelServidor
import mx.tec.familiasquesuman.domain.QueAvisar
import mx.tec.familiasquesuman.domain.TipoAviso
import mx.tec.familiasquesuman.domain.avisosDelServidor
import mx.tec.familiasquesuman.domain.juntarAvisos
import mx.tec.familiasquesuman.domain.ultimoAvisoDelServidor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** RF-16 / RF-22: los avisos que manda Familias que Suman y que el teléfono pide cada 15 minutos. */
class AvisosDelServidorTest {

    private val urgencia = AvisoDelServidor(7, urgencia = true, titulo = "Urgen cobijas", cuerpo = "Albergue Sendero", actividadId = null)
    private val nueva = AvisoDelServidor(8, urgencia = false, titulo = "Nueva actividad en Monterrey", cuerpo = "Posada", actividadId = "a1")

    @Test fun respetaLoQueLaFamiliaEligioEnAjustes() {
        val todo = avisosDelServidor(listOf(urgencia, nueva), QueAvisar(urgencias = true, favoritos = true))
        assertEquals(listOf(TipoAviso.URGENCIA, TipoAviso.FAVORITO), todo.map { it.tipo })
        assertTrue(avisosDelServidor(listOf(urgencia), QueAvisar(urgencias = false)).isEmpty())
        assertTrue(avisosDelServidor(listOf(nueva), QueAvisar(favoritos = false)).isEmpty())
    }

    @Test fun noLlegaDosVecesYSePideSoloLoQueSigue() {
        val avisos = avisosDelServidor(listOf(urgencia, nueva), QueAvisar(urgencias = true))
        val (bandeja, _) = juntarAvisos(emptyList(), avisos, reemplazar = false, ahora = 1_000)
        val (otraVez, llegaron) = juntarAvisos(bandeja, avisos, reemplazar = false, ahora = 2_000)
        assertTrue(llegaron.isEmpty())
        assertEquals(bandeja, otraVez)
        assertEquals(8L, ultimoAvisoDelServidor(bandeja))
        assertEquals(0L, ultimoAvisoDelServidor(emptyList()))
    }
}
