package mx.tec.familiasquesuman

import mx.tec.familiasquesuman.data.cifrarContrasena
import mx.tec.familiasquesuman.data.contrasenaCoincide
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrasenasTest {

    @Test
    fun la_contrasena_correcta_coincide_y_otra_no() {
        val cifrada = cifrarContrasena("familia123")
        assertTrue(contrasenaCoincide("familia123", cifrada))
        assertFalse(contrasenaCoincide("familia124", cifrada))
        assertFalse(contrasenaCoincide("", cifrada))
    }

    @Test
    fun no_se_guarda_el_texto_y_cada_cifrado_es_distinto() {
        val una = cifrarContrasena("familia123")
        val otra = cifrarContrasena("familia123")
        assertFalse(una.contains("familia123"))
        assertNotEquals(una, otra)
        assertTrue(contrasenaCoincide("familia123", otra))
    }

    @Test
    fun una_clave_mal_formada_no_deja_entrar() {
        assertFalse(contrasenaCoincide("familia123", "familia123"))
        assertFalse(contrasenaCoincide("familia123", "a:b:c"))
    }
}
