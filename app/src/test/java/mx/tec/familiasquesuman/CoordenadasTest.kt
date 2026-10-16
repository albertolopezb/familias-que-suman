package mx.tec.familiasquesuman

import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.coordenadaDeTexto
import mx.tec.familiasquesuman.ui.components.ubicacionValida
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** El campo "Ubicación en el mapa" de los formularios de administración. */
class CoordenadasTest {

    @Test
    fun coordenadas_como_las_copia_google_maps() {
        assertEquals(Coordenada(25.6694, -100.3098), coordenadaDeTexto("25.6694, -100.3098"))
        assertEquals(Coordenada(25.6694, -100.3098), coordenadaDeTexto("  25.6694,-100.3098 "))
    }

    @Test
    fun enlace_largo_usa_el_pin_exacto_antes_que_el_centro_de_la_vista() {
        val enlace = "https://www.google.com/maps/place/Macroplaza/@25.6700,-100.3100,17z/data=!3m1!4b1!4m6!3m5!3d25.6694!4d-100.3098"
        assertEquals(Coordenada(25.6694, -100.3098), coordenadaDeTexto(enlace))
    }

    @Test
    fun enlace_sin_pin_usa_el_centro_de_la_vista() {
        assertEquals(Coordenada(25.67, -100.31), coordenadaDeTexto("https://www.google.com/maps/@25.67,-100.31,15z"))
    }

    @Test
    fun lo_que_no_se_entiende_o_esta_fuera_de_rango_no_es_coordenada() {
        assertNull(coordenadaDeTexto("https://maps.app.goo.gl/AbCdEf123"))
        assertNull(coordenadaDeTexto("Av. Constitución 400"))
        assertNull(coordenadaDeTexto("95.0, -100.0"))
        assertNull(coordenadaDeTexto("25.0, -190.0"))
        assertNull(coordenadaDeTexto(""))
    }

    @Test
    fun vacio_es_valido_porque_significa_sin_lugar_en_el_mapa() {
        assertTrue(ubicacionValida(""))
        assertTrue(ubicacionValida("25.6694, -100.3098"))
        assertFalse(ubicacionValida("cerca del parque"))
    }

    @Test
    fun ida_y_vuelta_por_el_texto_del_formulario() {
        val original = Coordenada(25.6694, -100.3098)
        assertEquals(original, coordenadaDeTexto(original.comoTexto()))
    }
}
