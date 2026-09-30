package mx.tec.familiasquesuman.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Quién está usando la app: una familia o alguien del equipo de Familias que Suman.
 *
 * Es solo de prueba y vive en memoria, como todo en esta etapa. En el sitio el rol
 * viene del usuario que inició sesión (`role: "admin"`); cuando exista la cuenta
 * (etapa 3) esto se reemplaza por ese rol y las credenciales de abajo desaparecen.
 */
object SesionAdmin {

    /** Credenciales de demostración. NO son seguras: nada más sirven sin servidor. */
    const val CORREO_DEMO = "admin@familiasquesuman.com"
    const val CONTRASENA_DEMO = "admin123"

    var activa by mutableStateOf(false)
        private set

    var nombre by mutableStateOf("")
        private set

    /** Devuelve true si las credenciales coinciden con las de demostración. */
    fun entrar(correo: String, contrasena: String): Boolean {
        val valido = correo.trim().equals(CORREO_DEMO, ignoreCase = true) &&
            contrasena == CONTRASENA_DEMO
        if (valido) {
            activa = true
            nombre = "Equipo Familias que Suman"
        }
        return valido
    }

    fun salir() {
        activa = false
        nombre = ""
    }
}
