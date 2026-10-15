package mx.tec.familiasquesuman.data.sesion

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import mx.tec.familiasquesuman.data.red.CorreoCuerpo
import mx.tec.familiasquesuman.data.red.InicioSesionCuerpo
import mx.tec.familiasquesuman.data.red.ProveedorToken
import mx.tec.familiasquesuman.data.red.RegistroCuerpo
import mx.tec.familiasquesuman.data.red.TokenDto
import mx.tec.familiasquesuman.data.red.llamar
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * El inicio de sesión, con la API (backend/api/app/identidad.py).
 *
 * Cómo funciona (JWT, como en la guía de clase):
 *  1. La persona escribe correo y contraseña; la app los manda UNA vez a POST /v1/sesion/iniciar,
 *     por HTTPS. La contraseña no se guarda en ningún lado del teléfono.
 *  2. La API responde un JWT firmado (HS256) con el id de la cuenta y sus permisos. La app no
 *     puede (ni necesita) verificar la firma: el secreto solo lo tiene el servidor.
 *  3. El token se guarda CIFRADO con AES-256-GCM, con una llave que vive en el Android Keystore y
 *     nunca sale de ahí. Copiar el archivo de preferencias a otro teléfono no sirve.
 *  4. Cada llamada lo manda en `Authorization: Bearer …` (lo pone ClienteApi). Cuando vence, la
 *     app lo borra y pide volver a entrar.
 */
class ServicioSesion(contexto: Context) : ProveedorToken {

    private val prefs = contexto.applicationContext.getSharedPreferences("sesion_jwt", Context.MODE_PRIVATE)

    /** El token descifrado, en memoria, para no descifrar en cada petición. */
    @Volatile private var enMemoria: Pair<String, Long>? = null

    fun hayCredenciales(): Boolean = vigente() != null

    /** Correo y contraseña → token guardado. Si no coinciden, lanza ApiExcepcion (401). */
    suspend fun iniciar(correo: String, contrasena: String) {
        guardar(llamar { iniciarSesion(InicioSesionCuerpo(correo.trim(), contrasena)) })
    }

    /** Crea la cuenta y deja la sesión abierta (la API responde el token de una vez). */
    suspend fun registrarse(correo: String, contrasena: String, nombreFamilia: String) {
        guardar(llamar { registrarse(RegistroCuerpo(correo.trim(), contrasena, nombreFamilia.trim(), aceptaAviso = true)) })
    }

    /** Pide ayuda con la contraseña. El servidor responde lo mismo exista o no la cuenta. */
    suspend fun recuperar(correo: String) {
        llamar { recuperar(CorreoCuerpo(correo.trim())) }
    }

    override suspend fun token(): String? = vigente()

    /** Borra el token del teléfono. (El servidor lo invalida aparte: CuentaRepository.cerrarSesion.) */
    fun cerrar() {
        enMemoria = null
        prefs.edit().clear().apply()
    }

    private fun guardar(t: TokenDto) {
        val vence = System.currentTimeMillis() + t.expiraEn * 1000
        enMemoria = t.accessToken to vence
        prefs.edit().putString(TOKEN, Cifrado.cifrar(t.accessToken)).putLong(VENCE, vence).apply()
    }

    /** El token si todavía sirve (con un minuto de margen), o null. */
    private fun vigente(): String? {
        val (token, vence) = enMemoria ?: run {
            val guardado = prefs.getString(TOKEN, null) ?: return null
            val token = Cifrado.descifrar(guardado) ?: return null.also { cerrar() }
            (token to prefs.getLong(VENCE, 0)).also { enMemoria = it }
        }
        if (System.currentTimeMillis() > vence - 60_000) {
            cerrar()
            return null
        }
        return token
    }

    private companion object {
        const val TOKEN = "token"
        const val VENCE = "vence_ms"
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
