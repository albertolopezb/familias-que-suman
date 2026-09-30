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
 *
 * Primero se inicia sesión. Solo si la cuenta es de administrador el menú ofrece
 * cambiar a la vista de admin ([activa]); con una cuenta de familia no aparece nada.
 */
object SesionAdmin {

    /** Credenciales de demostración. NO son seguras: nada más sirven sin servidor. */
    const val CORREO_DEMO = "admin@familiasquesuman.com"
    const val CONTRASENA_DEMO = "admin123"
    const val CORREO_FAMILIA_DEMO = "ana.rodriguez@correo.com"
    const val CONTRASENA_FAMILIA_DEMO = "familia123"

    /** Correo de la cuenta con sesión iniciada; vacío si no hay sesión. */
    var correo by mutableStateOf("")
        private set

    var nombre by mutableStateOf("")
        private set

    /** La cuenta con sesión iniciada tiene rol de administrador. */
    var esCuentaAdmin by mutableStateOf(false)
        private set

    /** Se está usando la vista de admin (solo posible con una cuenta de admin). */
    var activa by mutableStateOf(false)
        private set

    val sesionIniciada: Boolean get() = correo.isNotEmpty()

    /** Devuelve true si las credenciales coinciden con alguna cuenta de demostración. */
    fun entrar(correo: String, contrasena: String): Boolean {
        val c = correo.trim()
        return when {
            c.equals(CORREO_DEMO, ignoreCase = true) && contrasena == CONTRASENA_DEMO -> {
                iniciar(CORREO_DEMO, "Equipo Familias que Suman", admin = true)
            }
            c.equals(CORREO_FAMILIA_DEMO, ignoreCase = true) && contrasena == CONTRASENA_FAMILIA_DEMO -> {
                iniciar(CORREO_FAMILIA_DEMO, "Familia Rodríguez", admin = false)
            }
            else -> false
        }
    }

    private fun iniciar(correo: String, nombre: String, admin: Boolean): Boolean {
        this.correo = correo
        this.nombre = nombre
        esCuentaAdmin = admin
        activa = false
        return true
    }

    /** Cambia a la vista de admin; solo funciona si la cuenta es de admin. */
    fun cambiarAVistaAdmin() {
        if (esCuentaAdmin) activa = true
    }

    /** Vuelve a la vista de familia sin cerrar la sesión. */
    fun salirDeVistaAdmin() {
        activa = false
    }

    fun cerrarSesion() {
        activa = false
        esCuentaAdmin = false
        correo = ""
        nombre = ""
    }
}
