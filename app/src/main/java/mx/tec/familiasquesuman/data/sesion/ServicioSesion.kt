package mx.tec.familiasquesuman.data.sesion

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.serialization.SerializationException
import mx.tec.familiasquesuman.data.red.ClienteApi
import mx.tec.familiasquesuman.data.red.CorreoCuerpo
import mx.tec.familiasquesuman.data.red.InicioSesionCuerpo
import mx.tec.familiasquesuman.data.red.ProveedorToken
import mx.tec.familiasquesuman.data.red.RegistroCuerpo
import mx.tec.familiasquesuman.data.red.RenovarCuerpo
import mx.tec.familiasquesuman.data.red.RestablecerCuerpo
import mx.tec.familiasquesuman.data.exigirExito
import mx.tec.familiasquesuman.data.red.TokenDto
import mx.tec.familiasquesuman.data.red.llamar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * El inicio de sesión, con la API (backend/api/app/identidad.py).
 *
 * Cómo funciona (JWT, como en la guía de clase, más refresh tokens):
 *  1. La persona escribe correo y contraseña; la app los manda UNA vez a POST /v1/sesion/iniciar,
 *     por HTTPS. La contraseña no se guarda en ningún lado del teléfono.
 *  2. La API responde dos cosas: un JWT firmado (HS256) que dura 15 minutos, con el id de la cuenta
 *     y sus permisos, y un refresh token que dura 30 días. La app no puede (ni necesita) verificar
 *     la firma del JWT: el secreto solo lo tiene el servidor.
 *  3. Los dos se guardan CIFRADOS con AES-256-GCM, con una llave que vive en el Android Keystore y
 *     nunca sale de ahí. Copiar el archivo de preferencias a otro teléfono no sirve.
 *  4. Cada llamada manda el JWT en `Authorization: Bearer …` (lo pone ClienteApi). Cuando está por
 *     vencer, o la API responde 401, la app lo cambia en POST /v1/sesion/renovar: entrega el refresh
 *     token y recibe un JWT nuevo Y un refresh nuevo (el viejo ya no sirve: rotación).
 *  5. Si la renovación es rechazada (se cerró la sesión, se cambió la contraseña, alguien reusó un
 *     refresh viejo), se borra todo y se pide volver a entrar.
 */
class ServicioSesion(contexto: Context) : ProveedorToken {

    private val prefs = contexto.applicationContext.getSharedPreferences("sesion_jwt", Context.MODE_PRIVATE)

    /** Lo de la sesión, descifrado, en memoria, para no descifrar en cada petición. */
    private data class Tokens(val acceso: String, val accesoVence: Long, val refresh: String?, val refreshVence: Long)

    @Volatile private var enMemoria: Tokens? = null

    /** Dos peticiones con el token vencido a la vez → una sola renovación (el refresh es de un uso). */
    private val renovando = Any()

    /** Hay con qué retomar la sesión sin contraseña: un JWT vigente o un refresh token vigente. */
    fun hayCredenciales(): Boolean {
        val t = guardados() ?: return false
        val ahora = System.currentTimeMillis()
        return ahora < t.accesoVence - MARGEN || (t.refresh != null && ahora < t.refreshVence - MARGEN)
    }

    /** Para cerrar sesión: el servidor revoca este refresh (y toda su familia), no solo el JWT. */
    fun refreshActual(): String? = guardados()?.refresh

    /** Correo y contraseña → tokens guardados. Si no coinciden, lanza ApiExcepcion (401). */
    suspend fun iniciar(correo: String, contrasena: String) {
        guardar(llamar { iniciarSesion(InicioSesionCuerpo(correo.trim(), contrasena)) })
    }

    /** Crea la cuenta y deja la sesión abierta (la API responde los tokens de una vez). */
    suspend fun registrarse(correo: String, contrasena: String, nombreFamilia: String) {
        guardar(llamar { registrarse(RegistroCuerpo(correo.trim(), contrasena, nombreFamilia.trim(), aceptaAviso = true)) })
    }

    /** Pide un código por correo. El servidor responde lo mismo exista o no la cuenta. */
    suspend fun recuperar(correo: String) {
        llamar { recuperar(CorreoCuerpo(correo.trim())) }
    }

    /**
     * El código del correo + la contraseña nueva. Si sirve, el servidor cierra TODAS las sesiones
     * de la cuenta (también esta, si hubiera): después se entra con la contraseña nueva.
     */
    suspend fun restablecer(correo: String, codigo: String, contrasenaNueva: String) {
        llamar { restablecer(RestablecerCuerpo(correo.trim(), codigo.trim(), contrasenaNueva)) }.exigirExito()
        cerrar()
    }

    /** El JWT para la siguiente petición; si está por vencer, primero lo renueva. */
    override fun token(): String? {
        val t = guardados() ?: return null
        if (System.currentTimeMillis() < t.accesoVence - MARGEN) return t.acceso
        return renovar(t.acceso)
    }

    /**
     * Cambia el refresh token por tokens nuevos. Se llama desde los hilos de OkHttp (bloquea).
     * [tokenUsado] es el JWT que falló: si mientras esperábamos otro hilo ya lo cambió, se usa el
     * nuevo sin gastar otro refresh.
     */
    override fun renovar(tokenUsado: String?): String? = synchronized(renovando) {
        val t = guardados() ?: return null
        if (tokenUsado != null && t.acceso != tokenUsado && System.currentTimeMillis() < t.accesoVence - MARGEN) {
            return t.acceso
        }
        val refresh = t.refresh
        if (refresh == null || System.currentTimeMillis() >= t.refreshVence) {
            cerrar()
            return null
        }
        val cuerpo = ClienteApi.json.encodeToString(RenovarCuerpo.serializer(), RenovarCuerpo(refresh))
            .toRequestBody("application/json; charset=UTF-8".toMediaType())
        val pedido = Request.Builder().url(ClienteApi.baseUrl + "v1/sesion/renovar").post(cuerpo).build()
        try {
            ClienteApi.httpSinSesion.newCall(pedido).execute().use { r ->
                when {
                    r.isSuccessful -> {
                        val nuevo = ClienteApi.json.decodeFromString(TokenDto.serializer(), r.body?.string().orEmpty())
                        guardar(nuevo)
                        nuevo.accessToken
                    }
                    // 401: el refresh ya no sirve (cerrado, vencido o reusado). 422: está mal formado.
                    r.code == 401 || r.code == 422 -> { cerrar(); null }
                    else -> null    // 429 o 5xx: se queda la sesión y se reintenta luego
                }
            }
        } catch (e: IOException) {
            null                    // sin red: igual, no se pierde la sesión
        } catch (e: SerializationException) {
            null
        }
    }

    /** Borra los tokens del teléfono. (El servidor los invalida aparte: CuentaRepository.cerrarSesion.) */
    fun cerrar() {
        enMemoria = null
        prefs.edit().clear().apply()
    }

    private fun guardar(t: TokenDto) {
        val ahora = System.currentTimeMillis()
        val nuevo = Tokens(t.accessToken, ahora + t.expiraEn * 1000, t.refreshToken, ahora + t.refreshExpiraEn * 1000)
        enMemoria = nuevo
        // commit (no apply): si la app se cierra justo después de rotar, el refresh viejo ya no sirve.
        prefs.edit()
            .putString(TOKEN, Cifrado.cifrar(nuevo.acceso)).putLong(VENCE, nuevo.accesoVence)
            .putString(REFRESH, Cifrado.cifrar(t.refreshToken)).putLong(REFRESH_VENCE, nuevo.refreshVence)
            .commit()
    }

    /** Lo guardado (de memoria o descifrado del disco), aunque ya haya vencido; null si no hay. */
    private fun guardados(): Tokens? = enMemoria ?: run {
        val acceso = prefs.getString(TOKEN, null)?.let(Cifrado::descifrar) ?: return null
        val refresh = prefs.getString(REFRESH, null)?.let(Cifrado::descifrar)
        Tokens(acceso, prefs.getLong(VENCE, 0), refresh, prefs.getLong(REFRESH_VENCE, 0)).also { enMemoria = it }
    }

    private companion object {
        const val TOKEN = "token"
        const val VENCE = "vence_ms"
        const val REFRESH = "refresh"
        const val REFRESH_VENCE = "refresh_vence_ms"
        /** Se renueva un minuto antes de que venza, por si el reloj del teléfono va un poco atrás. */
        const val MARGEN = 60_000L
    }
}

/** AES-256-GCM con una llave del Android Keystore (no se puede exportar ni leer desde la app). */
private object Cifrado {
    private const val ALIAS = "familias_sesion"
    private const val TRANSFORMACION = "AES/GCM/NoPadding"

    private fun llave(): SecretKey {
        val almacen = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (almacen.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    /** "iv:datos" en Base64. GCM además detecta si alguien cambió el texto cifrado. */
    fun cifrar(texto: String): String {
        val cifrador = Cipher.getInstance(TRANSFORMACION).apply { init(Cipher.ENCRYPT_MODE, llave()) }
        val datos = cifrador.doFinal(texto.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cifrador.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(datos, Base64.NO_WRAP)
    }

    /** null si no se puede (llave borrada, datos alterados): entonces simplemente no hay sesión. */
    fun descifrar(guardado: String): String? = runCatching {
        val (iv, datos) = guardado.split(":", limit = 2).map { Base64.decode(it, Base64.NO_WRAP) }
        val cifrador = Cipher.getInstance(TRANSFORMACION).apply { init(Cipher.DECRYPT_MODE, llave(), GCMParameterSpec(128, iv)) }
        String(cifrador.doFinal(datos), Charsets.UTF_8)
    }.getOrNull()
}
