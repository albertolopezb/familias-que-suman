package mx.tec.familiasquesuman.data.red

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import mx.tec.familiasquesuman.BuildConfig
import okhttp3.Authenticator
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Lo que respondió mal la API, con su `codigo` (lo que la app entiende) y su mensaje (lo que se
 * le enseña a la persona). `estado` 0 = no hubo respuesta (sin internet, servidor apagado).
 */
class ApiExcepcion(
    val estado: Int,
    val codigo: String,
    override val message: String,
    val datos: JsonObject? = null
) : Exception(message) {
    val sinConexion: Boolean get() = estado == 0
    fun datoEntero(nombre: String): Int? = datos?.get(nombre)?.jsonPrimitive?.intOrNull
}

/**
 * De dónde sale el token de la sesión. Lo implementa ServicioSesion. Se llama desde los hilos de
 * OkHttp (nunca desde el principal), así que puede tardar lo que tarde una renovación.
 */
interface ProveedorToken {
    /** El JWT vigente; si está por vencer, lo renueva antes. null = sin sesión. */
    fun token(): String?

    /** El servidor rechazó [tokenUsado]: pide uno nuevo con el refresh token. null = no se pudo. */
    fun renovar(tokenUsado: String?): String?
}

private object SinSesion : ProveedorToken {
    override fun token(): String? = null
    override fun renovar(tokenUsado: String?): String? = null
}

/**
 * Arma el cliente de la API: Retrofit para las rutas, OkHttp para la conexión, kotlinx.serialization
 * para el JSON.
 *
 * Seguridad del lado del teléfono:
 *  · En la versión de entrega, solo HTTPS (res/xml/seguridad_red.xml prohíbe HTTP en claro y
 *    solo confía en los certificados del sistema, no en los que instale el usuario).
 *  · El token viaja en el encabezado Authorization, nunca en la URL (las URLs se quedan en
 *    bitácoras de proxies y del servidor).
 *  · No se escribe en Logcat ni el cuerpo ni los encabezados: ahí irían el token y los datos.
 *  · Tiempos límite: una red lenta no deja la app colgada para siempre.
 */
object ClienteApi {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    /** La raíz de la API, siempre terminada en "/". */
    val baseUrl: String = BuildConfig.API_URL.trimEnd('/') + "/"

    @Volatile private var proveedor: ProveedorToken = SinSesion

    fun usarProveedorDeToken(nuevo: ProveedorToken) {
        proveedor = nuevo
    }

    private val conToken = Interceptor { cadena ->
        val original = cadena.request()
        // OkHttp corre esto en su propio hilo: aquí sí se puede esperar a que se descifre (o renueve).
        val token = runCatching { proveedor.token() }.getOrNull()
        val peticion = if (token.isNullOrBlank()) original
        else original.newBuilder().header("Authorization", "Bearer $token").build()
        cadena.proceed(peticion)
    }

    /**
     * Si la API responde 401 a una petición que llevaba token (venció, o el reloj del teléfono va
     * adelantado), se renueva UNA vez y se reintenta. Las rutas de sesión no pasan por aquí: un 401
     * al iniciar sesión es "contraseña equivocada", no "token vencido".
     */
    private val renovarAlRecibir401 = Authenticator { _, respuesta ->
        val pedido = respuesta.request
        val usado = pedido.header("Authorization")?.removePrefix("Bearer ")
        if (usado == null || respuesta.priorResponse != null || pedido.url.encodedPath.contains("/v1/sesion/")) {
            null
        } else {
            runCatching { proveedor.renovar(usado) }.getOrNull()
                ?.let { nuevo -> pedido.newBuilder().header("Authorization", "Bearer $nuevo").build() }
        }
    }

    /** Sin token ni Authenticator: solo para renovar la sesión (si no, se llamaría a sí mismo). */
    val httpSinSesion: OkHttpClient = OkHttpClient.Builder()
        // Solo TLS 1.3 y 1.2 con cifrados AEAD modernos (AES-GCM, ChaCha20-Poly1305): aunque el
        // servidor aceptara TLS 1.0/1.1 (el borde del Quick Tunnel todavía los acepta), la app no
        // baja. CLEARTEXT solo sirve en debug hacia 10.0.2.2; en release lo prohíbe seguridad_red.xml.
        .connectionSpecs(listOf(ConnectionSpec.RESTRICTED_TLS, ConnectionSpec.CLEARTEXT))
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /** Comparte conexiones con [httpSinSesion]; este sí pone el token y renueva al recibir 401. */
    val http: OkHttpClient = httpSinSesion.newBuilder()
        .addInterceptor(conToken)
        .authenticator(renovarAlRecibir401)
        .build()

    val api: ApiFamilias = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(http)
        .addConverterFactory(json.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
        .build()
        .create(ApiFamilias::class.java)

    /** La URL completa de algo que la API devolvió como ruta ("/v1/fotos/…"). */
    fun urlDe(ruta: String): String = baseUrl + ruta.trimStart('/')
}

/**
 * Hace una llamada a la API y convierte cualquier falla en [ApiExcepcion]. Las pantallas solo
 * tienen que cachar un tipo de error.
 */
suspend fun <T> llamar(bloque: suspend ApiFamilias.() -> T): T = try {
    ClienteApi.api.bloque()
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
    val error = cuerpo?.let { runCatching { ClienteApi.json.decodeFromString(ErrorDto.serializer(), it) }.getOrNull() }
    throw ApiExcepcion(
        estado = e.code(),
        codigo = error?.codigo?.ifBlank { null } ?: "http_${e.code()}",
        message = error?.error?.ifBlank { null } ?: "No se pudo completar. Intenta de nuevo.",
        datos = error?.datos
    )
} catch (e: IOException) {
    throw ApiExcepcion(0, "sin_conexion", "Revisa tu conexión a internet e intenta de nuevo.")
} catch (e: kotlinx.serialization.SerializationException) {
    throw ApiExcepcion(-1, "respuesta_invalida", "El servidor respondió algo que la app no entiende.")
}
