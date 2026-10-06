package mx.tec.familiasquesuman

import kotlinx.coroutines.runBlocking
import mx.tec.familiasquesuman.data.EncuestaRepository
import mx.tec.familiasquesuman.data.TestimonioRepository
import mx.tec.familiasquesuman.domain.EstadoTestimonio
import mx.tec.familiasquesuman.domain.MomentoEncuesta
import mx.tec.familiasquesuman.domain.RespuestaEncuesta
import mx.tec.familiasquesuman.domain.RespuestaPregunta
import mx.tec.familiasquesuman.domain.Testimonio
import org.junit.Assert.*
import org.junit.Test

class TestimoniosYEncuestasTest {

    private fun testimonio(id: String = "tx", participacion: String = "p1", correo: String = "a@correo.com") = Testimonio(
        id = id, participacionId = participacion, actividadTitulo = "Mega Limpieza", familia = "Familia A",
        correo = correo, experiencia = "Fue una tarde muy bonita en familia.", foto = "/foto.img", fecha = "5 oct 2026"
    )

    // ── RF-12: nada se publica sin aprobación ──

    @Test fun unTestimonioNuevoQuedaEnRevisionYNoSePublica() = runBlocking {
        val repo = TestimonioRepository()
        repo.enviar(testimonio())
        val enviado = repo.getDeFamilia("a@correo.com").single { it.participacionId == "p1" }
        assertEquals(EstadoTestimonio.EN_REVISION, enviado.estado)
        assertTrue(repo.getPublicados().none { it.participacionId == "p1" })
    }

    @Test fun noSePuedeEnviarUnTestimonioYaAprobado() = runBlocking {
        val repo = TestimonioRepository()
        repo.enviar(testimonio().copy(estado = EstadoTestimonio.APROBADO))
        assertTrue(repo.getPublicados().none { it.participacionId == "p1" })
    }

    @Test fun soloAprobarPublicaYAjustarDescartarONoLoHacen() = runBlocking {
        val repo = TestimonioRepository()
        repo.enviar(testimonio())
        val id = repo.getDeFamilia("a@correo.com").single { it.participacionId == "p1" }.id

        repo.cambiarEstado(id, EstadoTestimonio.AJUSTAR, "La foto no se ve bien")
        assertTrue(repo.getPublicados().none { it.id == id })
        repo.cambiarEstado(id, EstadoTestimonio.DESCARTADO)
        assertTrue(repo.getPublicados().none { it.id == id })
        repo.cambiarEstado(id, EstadoTestimonio.APROBADO)
        assertTrue(repo.getPublicados().any { it.id == id })
    }

    @Test fun ajustarGuardaLaNotaYReenviarVuelveARevisionSinNota() = runBlocking {
        val repo = TestimonioRepository()
        repo.enviar(testimonio())
        val id = repo.getDeFamilia("a@correo.com").single { it.participacionId == "p1" }.id
        repo.cambiarEstado(id, EstadoTestimonio.AJUSTAR, "Cuéntanos qué hicieron")
        assertEquals("Cuéntanos qué hicieron", repo.getTodos().single { it.id == id }.nota)

        repo.enviar(testimonio(id = "otro").copy(experiencia = "Ahora con más detalle de la tarde."))
        val reenviado = repo.getTodos().single { it.participacionId == "p1" && it.correo == "a@correo.com" }
        assertEquals(id, reenviado.id) // se reemplaza, no se duplica
        assertEquals(EstadoTestimonio.EN_REVISION, reenviado.estado)
        assertEquals("", reenviado.nota)
        assertEquals("Ahora con más detalle de la tarde.", reenviado.experiencia)
    }

    @Test fun losTestimoniosSeConsultanPorFamilia() = runBlocking {
        val repo = TestimonioRepository()
        repo.enviar(testimonio(correo = "a@correo.com", participacion = "p8"))
        repo.enviar(testimonio(correo = "b@correo.com", participacion = "p8"))
        assertEquals(1, repo.getDeFamilia("A@correo.com").count { it.participacionId == "p8" })
        assertEquals(2, repo.getTodos().count { it.participacionId == "p8" })
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
