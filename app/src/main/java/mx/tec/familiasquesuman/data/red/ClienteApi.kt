package mx.tec.familiasquesuman.data.red

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import mx.tec.familiasquesuman.BuildConfig
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

/** De dónde sale el token de la sesión. Lo implementa ServicioSesion. */
fun interface ProveedorToken {
    suspend fun token(): String?
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

    @Volatile private var proveedor: ProveedorToken = ProveedorToken { null }

    fun usarProveedorDeToken(nuevo: ProveedorToken) {
        proveedor = nuevo
    }

    private val conToken = Interceptor { cadena ->
        val original = cadena.request()
        // OkHttp corre esto en su propio hilo: aquí sí se puede esperar a que se descifre el token.
        val token = runCatching { runBlocking { proveedor.token() } }.getOrNull()
        val peticion = if (token.isNullOrBlank()) original
        else original.newBuilder().header("Authorization", "Bearer $token").build()
        cadena.proceed(peticion)
    }

    val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(conToken)
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
