package mx.tec.familiasquesuman.domain

data class Usuario(
    val correo: String,
    val esAdmin: Boolean,
    val nombreFamilia: String,
    val ciudad: String
)

object UsuariosHardcodeados {
    val USUARIO_NORMAL = Usuario(
        correo = "ana.rodriguez@correo.com",
        esAdmin = false,
        nombreFamilia = "Familia Rodríguez",
        ciudad = "Monterrey"
    )

    val USUARIO_ADMIN = Usuario(
        correo = "admin@familiasquesuman.org",
        esAdmin = true,
        nombreFamilia = "Administrador del Sistema",
        ciudad = "Monterrey"
    )
}