package mx.tec.familiasquesuman

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import mx.tec.familiasquesuman.data.local.CatalogoLocal
import mx.tec.familiasquesuman.data.local.RespuestaGuardada
import mx.tec.familiasquesuman.data.local.RespuestasDao
import mx.tec.familiasquesuman.data.red.ApiExcepcion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * El catálogo sin conexión (RF-29: consultar fichas sin internet). La tabla de Room
 * se cambia por una en memoria: lo que se prueba es la regla, no SQLite.
 */
class CatalogoLocalTest {

    private class TablaEnMemoria : RespuestasDao {
        val filas = mutableMapOf<String, RespuestaGuardada>()
        override suspend fun leer(clave: String) = filas[clave]
        override suspend fun guardar(respuesta: RespuestaGuardada) { filas[respuesta.clave] = respuesta }
    }

    private val lista = ListSerializer(String.serializer())
    private lateinit var tabla: TablaEnMemoria
    private val sinRed = ApiExcepcion(0, "sin_conexion", "Sin conexión")

    @Before
    fun preparar() {
        tabla = TablaEnMemoria()
        CatalogoLocal.usar(tabla)
    }

    @Test
    fun con_red_se_guarda_lo_que_llego_y_no_se_avisa_nada() = runBlocking {
        val r = CatalogoLocal.conRespaldo("actividades", lista) { listOf("Posada", "Limpieza") }

        assertEquals(listOf("Posada", "Limpieza"), r)
        assertNotNull(tabla.filas["actividades"])
        assertNull(CatalogoLocal.mostrandoGuardado.value)
    }

    @Test
    fun sin_red_sale_lo_ultimo_guardado_y_se_avisa_de_cuando_es() = runBlocking {
        CatalogoLocal.conRespaldo("actividades", lista) { listOf("Posada") }

        val r = CatalogoLocal.conRespaldo("actividades", lista) { throw sinRed }

        assertEquals(listOf("Posada"), r)
        assertNotNull("La app debe avisar que es lo guardado", CatalogoLocal.mostrandoGuardado.value)
    }

    @Test
    fun al_volver_la_red_desaparece_el_aviso_y_se_reemplaza_lo_guardado() = runBlocking {
        CatalogoLocal.conRespaldo("actividades", lista) { listOf("Posada") }
        CatalogoLocal.conRespaldo("actividades", lista) { throw sinRed }

        val r = CatalogoLocal.conRespaldo("actividades", lista) { listOf("Posada", "Nueva") }

        assertEquals(listOf("Posada", "Nueva"), r)
        assertNull(CatalogoLocal.mostrandoGuardado.value)
        assertTrue(tabla.filas["actividades"]!!.json.contains("Nueva"))
    }

    @Test
    fun sin_red_y_sin_nada_guardado_el_error_se_ve_tal_cual() = runBlocking {
        try {
            CatalogoLocal.conRespaldo("proyectos", lista) { throw sinRed }
            fail("Debió dejar pasar el error")
        } catch (e: ApiExcepcion) {
            assertTrue(e.sinConexion)
        }
    }

    @Test
    fun un_error_del_servidor_no_se_tapa_con_lo_guardado() = runBlocking {
        CatalogoLocal.conRespaldo("actividad/1", String.serializer()) { "Posada" }
        try {
            CatalogoLocal.conRespaldo("actividad/1", String.serializer()) { throw ApiExcepcion(404, "no_encontrado", "No existe") }
            fail("Un 404 tiene que verse: la actividad ya no existe")
        } catch (e: ApiExcepcion) {
            assertEquals(404, e.estado)
        }
    }

    @Test
    fun con_el_servidor_caido_tambien_sale_lo_guardado() = runBlocking {
        CatalogoLocal.conRespaldo("proyectos", lista) { listOf("Bibliotecas Infantiles") }

        val r = CatalogoLocal.conRespaldo("proyectos", lista) { throw ApiExcepcion(502, "http_502", "Bad gateway") }

        assertEquals(listOf("Bibliotecas Infantiles"), r)
        assertNotNull(CatalogoLocal.mostrandoGuardado.value)
    }
}
