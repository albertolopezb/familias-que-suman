package mx.tec.familiasquesuman

import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.destacadas
import org.junit.Assert.assertEquals
import org.junit.Test

/** "Causas destacadas" del Inicio: las elige la administración; si no eligió ninguna, las dos primeras. */
class DestacadasTest {

    private fun causa(nombre: String, destacada: Boolean = false) =
        Asociacion(nombre, nombre, "Niñez", "", "", "", "", "", destacada = destacada)

    @Test fun salenLasQueDestacoLaAdministracion() {
        val lista = listOf(causa("A"), causa("B", true), causa("C"), causa("D", true))
        assertEquals(listOf("B", "D"), lista.destacadas().map { it.nombre })
    }

    @Test fun sinNingunaDestacadaSalenLasDosPrimerasYNuncaMasDeTres() {
        assertEquals(listOf("A", "B"), listOf(causa("A"), causa("B"), causa("C")).destacadas().map { it.nombre })
        assertEquals(3, List(5) { causa("x$it", true) }.destacadas().size)
    }
}
