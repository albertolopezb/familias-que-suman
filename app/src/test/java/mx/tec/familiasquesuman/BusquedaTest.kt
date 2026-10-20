package mx.tec.familiasquesuman

import mx.tec.familiasquesuman.ui.components.coincideBusqueda
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** El buscador de Actividades y Donar: sin distinguir mayúsculas ni acentos, en cualquier campo. */
class BusquedaTest {

    @Test
    fun sin_busqueda_todo_coincide() {
        assertTrue(coincideBusqueda("", "Posada"))
        assertTrue(coincideBusqueda("   ", "Posada"))
    }

    @Test
    fun ignora_mayusculas_y_acentos_en_los_dos_lados() {
        assertTrue(coincideBusqueda("bernabe", "Comedor Comunitario San Bernabé"))
        assertTrue(coincideBusqueda("BERNABÉ", "comedor san bernabe"))
        assertTrue(coincideBusqueda("utiles", "Útiles escolares"))
    }

    @Test
    fun busca_en_cualquiera_de_los_textos_y_tolera_nulos() {
        assertTrue(coincideBusqueda("despensas", null, "Título", "Armar despensas"))
        assertFalse(coincideBusqueda("juguetes", null, "Despensas", "Ropa"))
    }
}
