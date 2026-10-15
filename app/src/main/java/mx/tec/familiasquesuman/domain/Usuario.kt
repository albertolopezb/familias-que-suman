package mx.tec.familiasquesuman.domain

/**
 * Quién usa la app, como lo ven las pantallas. `esAdmin` lo decide el SERVIDOR (los permisos que
 * la API firma dentro del token); la app solo lo usa para enseñar u ocultar botones. Antes se adivinaba
 * por el correo (cualquier correo con "admin" era administrador): eso ya no existe.
 */
data class Usuario(
    val correo: String,
    val esAdmin: Boolean,
    val nombreFamilia: String,
    val ciudad: String
) {
    companion object {
        /** Sin sesión: nadie es admin. */
        val INVITADO = Usuario(correo = "", esAdmin = false, nombreFamilia = "", ciudad = "Monterrey")
    }
}
