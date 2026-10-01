package mx.tec.familiasquesuman.domain

data class Usuario(
    val correo: String,
    val esAdmin: Boolean,
    val nombreFamilia: String,
    val ciudad: String
)

// En domain/Usuario.kt
object UsuariosHardcodeados {
    val USUARIO_NORMAL = Usuario(
        correo = "ana.rodriguez@correo.com",
        esAdmin = false,
        nombreFamilia = "Familia Rodríguez",
        ciudad = "Monterrey"
    )

    val USUARIO_ADMIN = Usuario(
        correo = "admin@correo.com",
        esAdmin = true,
        nombreFamilia = "Administrador General",
        ciudad = "Monterrey"
    )

    fun obtenerPorCorreo(correo: String?): Usuario {
        if (correo == null) return USUARIO_NORMAL
        val correoLimpio = correo.trim().lowercase()
        return if (correoLimpio == USUARIO_ADMIN.correo || correoLimpio.contains("admin")) {
            USUARIO_ADMIN
        } else {
            USUARIO_NORMAL
        }
    }
}