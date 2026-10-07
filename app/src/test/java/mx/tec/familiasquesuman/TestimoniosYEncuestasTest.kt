package mx.tec.familiasquesuman

import kotlinx.coroutines.runBlocking
import mx.tec.familiasquesuman.data.EncuestaRepository
import mx.tec.familiasquesuman.data.TestimonioRepository
import mx.tec.familiasquesuman.domain.MomentoEncuesta
import mx.tec.familiasquesuman.domain.RespuestaEncuesta
import mx.tec.familiasquesuman.domain.RespuestaPregunta
import mx.tec.familiasquesuman.domain.Testimonio
import mx.tec.familiasquesuman.domain.puedeEliminarlo
import org.junit.Assert.*
import org.junit.Test

class TestimoniosYEncuestasTest {

    private fun testimonio(id: String = "tx", participacion: String = "p1", correo: String = "a@correo.com") = Testimonio(
        id = id, participacionId = participacion, actividadTitulo = "Mega Limpieza", familia = "Familia A",
        correo = correo, experiencia = "Fue una tarde muy bonita en familia.", foto = null, fecha = "5 oct 2026"
    )

    // ── RF-12: se publica al enviar y se puede eliminar, solo por su autora o autor o por el admin ──

    @Test fun unTestimonioSePublicaAlEnviarse() = runBlocking {
        val repo = TestimonioRepository()
        repo.publicar(testimonio(participacion = "p9"))
        assertTrue(repo.getTodos().any { it.participacionId == "p9" })
    }

    @Test fun publicarDeNuevoLaMismaParticipacionLaReemplaza() = runBlocking {
        val repo = TestimonioRepository()
        repo.publicar(testimonio(id = "a", participacion = "p9"))
        repo.publicar(testimonio(id = "b", participacion = "p9").copy(experiencia = "Ahora lo conté mejor, con más detalle."))
        val propios = repo.getDeFamilia("a@correo.com").filter { it.participacionId == "p9" }
        assertEquals(1, propios.size)
        assertEquals("Ahora lo conté mejor, con más detalle.", propios.single().experiencia)
    }

    @Test fun laAutoraPuedeEliminarSuTestimonio() = runBlocking {
        val repo = TestimonioRepository()
        repo.publicar(testimonio(participacion = "p9"))
        val id = repo.getDeFamilia("a@correo.com").single { it.participacionId == "p9" }.id
        assertTrue(repo.eliminar(id, "A@Correo.com", esAdmin = false))
        assertTrue(repo.getTodos().none { it.id == id })
    }

    @Test fun otraFamiliaOUnVisitanteNoPuedenEliminarTestimoniosAjenos() = runBlocking {
        val repo = TestimonioRepository()
        repo.publicar(testimonio(participacion = "p9"))
        val id = repo.getDeFamilia("a@correo.com").single { it.participacionId == "p9" }.id
        assertFalse(repo.eliminar(id, "otra@correo.com", esAdmin = false))
        assertFalse(repo.eliminar(id, null, esAdmin = false))
        assertTrue(repo.getTodos().any { it.id == id })
    }

    @Test fun elAdminPuedeEliminarCualquierTestimonio() = runBlocking {
        val repo = TestimonioRepository()
        repo.publicar(testimonio(participacion = "p9"))
        val id = repo.getDeFamilia("a@correo.com").single { it.participacionId == "p9" }.id
        assertTrue(repo.eliminar(id, "admin@correo.com", esAdmin = true))
        assertTrue(repo.getTodos().none { it.id == id })
    }

    @Test fun eliminarUnoQueNoExisteNoHaceNada() = runBlocking {
        val repo = TestimonioRepository()
        val antes = repo.getTodos().size
        assertFalse(repo.eliminar("no-existe", "admin@correo.com", esAdmin = true))
        assertEquals(antes, repo.getTodos().size)
    }

    @Test fun despuesDeEliminarSePuedeCompartirOtroDeLaMismaActividad() = runBlocking {
        val repo = TestimonioRepository()
        repo.publicar(testimonio(participacion = "p9"))
        val id = repo.getDeFamilia("a@correo.com").single { it.participacionId == "p9" }.id
        repo.eliminar(id, "a@correo.com", esAdmin = false)
        repo.publicar(testimonio(id = "nuevo", participacion = "p9"))
        assertEquals(1, repo.getDeFamilia("a@correo.com").count { it.participacionId == "p9" })
    }

    @Test fun laReglaDeQuienPuedeEliminarEsLaMisma() {
        val t = testimonio(correo = "a@correo.com")
        assertTrue(t.puedeEliminarlo("a@correo.com", esAdmin = false))
        assertTrue(t.puedeEliminarlo("x@correo.com", esAdmin = true))
        assertFalse(t.puedeEliminarlo("x@correo.com", esAdmin = false))
        assertFalse(t.puedeEliminarlo(null, esAdmin = false))
    }

    // ── RF-13: las respuestas quedan asociadas a la actividad y se consultan ──

    private fun respuesta(actividad: String, momento: MomentoEncuesta, correo: String = "a@correo.com", texto: String = "Sí") =
        RespuestaEncuesta(
            id = "e-$actividad-$momento-$correo-$texto", actividadId = actividad, actividadTitulo = "Actividad $actividad",
            correo = correo, familia = "Familia", momento = momento,
            respuestas = listOf(RespuestaPregunta(1, "¿Pregunta?", texto)), fecha = "5 oct 2026"
        )

    @Test fun lasRespuestasSeConsultanPorActividadYPorFamilia() = runBlocking {
        val repo = EncuestaRepository()
        repo.guardar(respuesta("act9", MomentoEncuesta.ANTES))
        repo.guardar(respuesta("act9", MomentoEncuesta.DESPUES))
        repo.guardar(respuesta("act9", MomentoEncuesta.ANTES, correo = "b@correo.com"))

        assertEquals(3, repo.getPorActividad("act9").size)
        assertEquals(2, repo.getDeFamilia("a@correo.com").count { it.actividadId == "act9" })
        assertTrue(repo.getTodas().size >= 3)
    }

    @Test fun contestarDeNuevoLaMismaEncuestaLaReemplaza() = runBlocking {
        val repo = EncuestaRepository()
        repo.guardar(respuesta("act7", MomentoEncuesta.ANTES, texto = "Sí"))
        repo.guardar(respuesta("act7", MomentoEncuesta.ANTES, texto = "No"))
        val guardadas = repo.getPorActividad("act7")
        assertEquals(1, guardadas.size)
        assertEquals("No", guardadas.single().respuestas.single().respuesta)
    }
}
