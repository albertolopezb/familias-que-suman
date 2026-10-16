package mx.tec.familiasquesuman

import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.porCercania
import org.junit.Assert.assertEquals
import org.junit.Test

/** RF-02 "Cerca de mí": se ordena en el teléfono, con la distancia en línea recta. */
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
}
