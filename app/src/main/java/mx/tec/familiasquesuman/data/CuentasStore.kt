package mx.tec.familiasquesuman.data

import android.content.Context
import mx.tec.familiasquesuman.domain.Acompanante
import mx.tec.familiasquesuman.domain.Sexo
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDate
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Lo que se guarda de cada cuenta: quién es, su familia y a qué actividades va. */
data class CuentaLocal(
    val correo: String,
    val familia: String,
    val titular: Acompanante,
    /** La contraseña cifrada (ver [cifrarContrasena]); nunca el texto que se escribió. */
    val clave: String,
    val acompanantes: List<Acompanante>,
    /** actividadId → acompañantes que van (sin contar al titular). */
    val inscripciones: Map<String, List<Acompanante>>
)

/**
 * Las cuentas, la sesión abierta y los lugares que movieron las inscripciones, guardados en el
 * teléfono: así la sesión sigue abierta al volver a abrir la app y una cuenta creada aquí puede
 * volver a entrar después de cerrar sesión. Con el backend esto lo guarda el servidor.
 */
class CuentasStore(contexto: Context) {

    private val prefs = contexto.applicationContext.getSharedPreferences("cuentas", Context.MODE_PRIVATE)

    private fun clave(correo: String) = correo.trim().lowercase()

    fun todas(): List<CuentaLocal> = prefs.all.keys
        .filter { it.startsWith("cuenta:") }
        .mapNotNull { llave -> prefs.getString(llave, null)?.let { leerCuenta(it) } }

    fun guardar(cuenta: CuentaLocal) {
        prefs.edit().putString("cuenta:${clave(cuenta.correo)}", escribirCuenta(cuenta).toString()).apply()
    }

    /** El correo de la cuenta con sesión abierta, o null si nadie la tiene. */
    fun correoSesion(): String? = prefs.getString("sesion", null)

    fun guardarSesion(correo: String?) {
        val cambio = prefs.edit()
        if (correo == null) cambio.remove("sesion") else cambio.putString("sesion", clave(correo))
        cambio.apply()
    }

    fun ajusteLugares(): Map<String, Int> = runCatching {
        val json = JSONObject(prefs.getString("ajuste_lugares", null) ?: return emptyMap())
        json.keys().asSequence().associateWith { json.getInt(it) }
    }.getOrDefault(emptyMap())

    fun guardarAjusteLugares(ajuste: Map<String, Int>) {
        prefs.edit().putString("ajuste_lugares", JSONObject(ajuste).toString()).apply()
    }

    private fun escribirCuenta(c: CuentaLocal) = JSONObject()
        .put("correo", c.correo)
        .put("familia", c.familia)
        .put("titular", escribirPersona(c.titular))
        .put("clave", c.clave)
        .put("acompanantes", JSONArray(c.acompanantes.map { escribirPersona(it) }))
        .put("inscripciones", JSONObject().apply {
            c.inscripciones.forEach { (actividad, van) -> put(actividad, JSONArray(van.map { escribirPersona(it) })) }
        })

    /** Una cuenta guardada con otra versión de la app que ya no se puede leer se ignora. */
    private fun leerCuenta(texto: String): CuentaLocal? = runCatching {
        val json = JSONObject(texto)
        val inscripciones = json.getJSONObject("inscripciones")
        CuentaLocal(
            correo = json.getString("correo"),
            familia = json.getString("familia"),
            titular = leerPersona(json.getJSONObject("titular")),
            clave = json.getString("clave"),
            acompanantes = leerPersonas(json.getJSONArray("acompanantes")),
            inscripciones = inscripciones.keys().asSequence()
                .associateWith { leerPersonas(inscripciones.getJSONArray(it)) }
        )
    }.getOrNull()

    private fun escribirPersona(p: Acompanante) = JSONObject()
        .put("nombre", p.nombre)
        .put("edad", p.edad)
        .put("nacimiento", p.fechaNacimiento?.toString())
        .put("sexo", p.sexo?.name)

    private fun leerPersonas(arreglo: JSONArray) = (0 until arreglo.length()).map { leerPersona(arreglo.getJSONObject(it)) }

    private fun leerPersona(json: JSONObject) = Acompanante(
        nombre = json.getString("nombre"),
        edad = json.optInt("edad"),
        fechaNacimiento = json.optString("nacimiento").takeIf { it.isNotEmpty() }?.let(LocalDate::parse),
        sexo = json.optString("sexo").takeIf { it.isNotEmpty() }?.let { runCatching { Sexo.valueOf(it) }.getOrNull() }
    )
}

/** Cifra la contraseña con una sal al azar: "sal:huella". Dos cuentas con la misma no se parecen. */
fun cifrarContrasena(contrasena: String): String {
    val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }.aHex()
    return "$sal:${huella(sal, contrasena)}"
}

/** true si [contrasena] es la que se cifró en [cifrada]. */
fun contrasenaCoincide(contrasena: String, cifrada: String): Boolean {
    val partes = cifrada.split(":")
    if (partes.size != 2) return false
    return MessageDigest.isEqual(huella(partes[0], contrasena).toByteArray(), partes[1].toByteArray())
}

/** PBKDF2: lento a propósito, para que adivinar la contraseña a partir de la huella cueste. */
private fun huella(sal: String, contrasena: String): String =
    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        .generateSecret(PBEKeySpec(contrasena.toCharArray(), sal.toByteArray(), 10_000, 256))
        .encoded.aHex()

private fun ByteArray.aHex() = joinToString("") { "%02x".format(it) }
