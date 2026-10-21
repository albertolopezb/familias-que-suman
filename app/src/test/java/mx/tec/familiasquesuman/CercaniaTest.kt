package mx.tec.familiasquesuman

import kotlinx.coroutines.runBlocking
import mx.tec.familiasquesuman.data.BuscadorDeLugares
import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.municipioConocido
import mx.tec.familiasquesuman.domain.porCercania
import mx.tec.familiasquesuman.domain.textoDeDistancia
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

/** RF-02 "Buscar cerca de": se ordena en el teléfono, con la distancia en línea recta, desde un lugar que se escribe. */
class CercaniaTest {

    private val macroplaza = Coordenada(25.6694, -100.3098)
    private val sanPedro = Coordenada(25.6573, -100.4026)
    private val hermosillo = Coordenada(29.0729, -110.9559)

    @Test fun laDistanciaEsRazonable() {
        assertEquals(0.0, macroplaza.distanciaKm(macroplaza), 1e-9)
        assertEquals(9.4, macroplaza.distanciaKm(sanPedro), 0.5)
        assertEquals(1130.0, macroplaza.distanciaKm(hermosillo), 25.0)
        assertEquals(macroplaza.distanciaKm(sanPedro), sanPedro.distanciaKm(macroplaza), 1e-9)
    }

    @Test fun ordenaDeLaMasCercanaALaMasLejanaYLoSinUbicacionAlFinal() {
        val lugares = listOf("sin ubicación A" to null, "Hermosillo" to hermosillo, "sin ubicación B" to null,
            "San Pedro" to sanPedro, "Macroplaza" to macroplaza)
        val orden = lugares.porCercania(Coordenada(25.67, -100.31)) { it.second }.map { it.first }
        assertEquals(listOf("Macroplaza", "San Pedro", "Hermosillo", "sin ubicación A", "sin ubicación B"), orden)
    }

    @Test fun laDistanciaSeLeeComoLaDiriaUnaPersona() {
        assertEquals("a 50 m", textoDeDistancia(0.01))
        assertEquals("a 850 m", textoDeDistancia(0.87))
        assertEquals("a 2.3 km", textoDeDistancia(2.34))
        assertEquals("a 14 km", textoDeDistancia(14.6))
    }

    @Test fun losMunicipiosSeEncuentranSinInternetYSinImportarAcentos() {
        assertEquals("Apodaca", municipioConocido("Apodaca")?.nombre)
        assertEquals("Apodaca", municipioConocido("  apodaca, N.L. ")?.nombre)
        assertEquals("San Nicolás de los Garza", municipioConocido("San Nicolás")?.nombre)
        assertEquals("San Pedro Garza García", municipioConocido("san pedro garza garcia, nuevo león")?.nombre)
        assertEquals("Hermosillo", municipioConocido("Hermosillo, Sonora")?.nombre)
        assertNull(municipioConocido("Colonia Mitras Centro"))
        assertNull(municipioConocido(""))
    }

    @Test fun laFamiliaDeApodacaVeLoDeApodacaAntesQueLoDelCentro() {
        val apodaca = municipioConocido("Apodaca")!!.coordenada
        val enApodaca = Coordenada(25.7790, -100.1950)
        val orden = listOf("Centro" to macroplaza, "Apodaca" to enApodaca).porCercania(apodaca) { it.second }.map { it.first }
        assertEquals(listOf("Apodaca", "Centro"), orden)
    }

    /** Un Nominatim de mentira en la computadora: lo que importa es qué se le manda y qué se entiende de vuelta. */
    @Test fun loDemasSeBuscaEnOpenStreetMapSinTokenYConNombreDeLaApp() = runBlocking {
        val pedidos = mutableListOf<Map<String, String?>>()
        val servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/search") { intercambio ->
                pedidos += mapOf(
                    "consulta" to intercambio.requestURI.rawQuery,
                    "autorizacion" to intercambio.requestHeaders.getFirst("Authorization"),
                    "agente" to intercambio.requestHeaders.getFirst("User-Agent")
                )
                val cuerpo = """[{"lat":"25.6950","lon":"-100.3480","display_name":"Mitras Centro, Monterrey, Nuevo León, México","otro":1}]"""
                    .toByteArray()
                intercambio.sendResponseHeaders(200, cuerpo.size.toLong())
                intercambio.responseBody.use { it.write(cuerpo) }
            }
            start()
        }
        try {
            val buscador = BuscadorDeLugares(OkHttpClient(), "http://127.0.0.1:${servidor.address.port}/search")
            val lugar = buscador.buscar("Col. Mitras Centro", "Monterrey")
            assertNotNull(lugar)
            assertEquals("Mitras Centro, Monterrey", lugar!!.nombre)
            assertEquals(25.6950, lugar.coordenada.latitud, 1e-9)
            assertEquals(1, pedidos.size)
            assertNull("no se manda el token de la app", pedidos[0]["autorizacion"])
            assertEquals(true, pedidos[0]["agente"]?.startsWith("FamiliasQueSuman"))
            assertEquals(true, pedidos[0]["consulta"]?.contains("countrycodes=mx"))

            // Un municipio no sale de la app.
            assertEquals("Apodaca", buscador.buscar("Apodaca", "Monterrey")?.nombre)
            assertEquals(1, pedidos.size)
            // Muy corto: ni se intenta.
            assertNull(buscador.buscar("ab", "Monterrey"))
            assertEquals(1, pedidos.size)
        } finally {
            servidor.stop(0)
        }
    }
}
