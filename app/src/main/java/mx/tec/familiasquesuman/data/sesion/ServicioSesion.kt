package mx.tec.familiasquesuman.data.sesion

import android.app.Activity
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import mx.tec.familiasquesuman.BuildConfig
import mx.tec.familiasquesuman.data.red.ProveedorToken

/**
 * El inicio de sesión, con Auth0.
 *
 * Cómo funciona (OAuth 2.0 para apps nativas, RFC 8252, con PKCE):
 *  1. La app abre la página de Auth0 en una pestaña del navegador (Custom Tab), NO en un
 *     formulario propio: la contraseña la escribe la persona en la página de Auth0 y la app nunca
 *     la ve. Así, ni una app maliciosa que la imite ni un error nuestro pueden filtrarla.
 *  2. PKCE: antes de abrir la página, el SDK inventa un secreto al azar (code_verifier) y manda
 *     solo su huella SHA-256 (code_challenge). Cuando Auth0 regresa a la app con un código, el SDK
 *     lo cambia por los tokens presentando el secreto. Si otra app interceptara el regreso, no
 *     tendría el secreto y el código no le serviría.
 *  3. Los tokens se guardan con SecureCredentialsManager: cifrados (AES-GCM) con una llave que
 *     vive en el Android Keystore y nunca sale de ahí. Copiar el archivo a otro teléfono no sirve.
 *  4. El access token dura poco (1 h); antes de que venza, el SDK pide otro con el refresh token.
 *     Con "Refresh Token Rotation" prendido en Auth0 (ver la guía), cada refresh token sirve una
 *     sola vez: si alguien robara uno viejo, ya no sirve.
 */
class ServicioSesion(contexto: Context) : ProveedorToken {

    private val app = contexto.applicationContext

    /** false mientras no se pongan auth0.dominio y auth0.cliente en local.properties. */
    val configurada: Boolean = BuildConfig.AUTH0_DOMINIO.isNotBlank() && BuildConfig.AUTH0_CLIENTE.isNotBlank()

    private val auth0 by lazy { Auth0.getInstance(BuildConfig.AUTH0_CLIENTE, BuildConfig.AUTH0_DOMINIO) }
    private val autenticacion by lazy { AuthenticationAPIClient(auth0) }
    private val credenciales by lazy {
        SecureCredentialsManager(autenticacion, app, SharedPreferencesStorage(app, "sesion_auth0"))
    }

    fun hayCredenciales(): Boolean = configurada && runCatching { credenciales.hasValidCredentials() }.getOrDefault(false)

    /**
     * Abre la página de Auth0 para entrar (o para crear cuenta con [registro]). [correo] solo
     * rellena el campo. `prompt=login`: siempre pide la contraseña, aunque el navegador recuerde
     * a otra persona (teléfonos compartidos en familia).
     */
    suspend fun iniciar(actividad: Activity, registro: Boolean, correo: String?): Credentials {
        check(configurada) { "Auth0 no está configurado" }
        val parametros = buildMap {
            put("prompt", "login")
            if (registro) put("screen_hint", "signup")
            if (!correo.isNullOrBlank()) put("login_hint", correo.trim())
        }
        val nuevas = WebAuthProvider.login(auth0)
            .withScheme(BuildConfig.AUTH0_ESQUEMA)
            .withAudience(BuildConfig.AUTH0_AUDIENCIA)
            .withScope("openid profile email offline_access")
            .withParameters(parametros)
            .await(actividad)
        credenciales.saveCredentials(nuevas)
        return nuevas
    }

    /** El access token vigente (renovado si ya casi vence), o null sin sesión. */
    override suspend fun token(): String? {
        if (!hayCredenciales()) return null
        return runCatching { credenciales.awaitCredentials().accessToken }.getOrNull()
    }

    /** Cierra la sesión en el teléfono: revoca el refresh token en Auth0 y borra todo lo guardado. */
    suspend fun cerrar() {
        if (!configurada) return
        runCatching {
            credenciales.awaitCredentials().refreshToken?.let { autenticacion.revokeToken(it).await() }
        }
        runCatching { credenciales.clearAll() }
    }

    /** Auth0 manda el correo para poner una contraseña nueva (responde igual exista o no la cuenta). */
    suspend fun recuperar(correo: String) {
        check(configurada) { "Auth0 no está configurado" }
        autenticacion.resetPassword(correo.trim(), CONEXION).await()
    }

    private companion object {
        /** La base de datos de usuarios de Auth0 (la que crea por omisión cada tenant). */
        const val CONEXION = "Username-Password-Authentication"
    }
}
