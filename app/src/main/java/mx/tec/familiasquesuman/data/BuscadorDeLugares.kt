package mx.tec.familiasquesuman.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.LugarDeReferencia
import mx.tec.familiasquesuman.domain.municipioConocido
import mx.tec.familiasquesuman.data.red.ClienteApi
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * "Buscar cerca de": convierte lo que escribe la persona ("Apodaca", "Col. Mitras", "Av. Garza
 * Sada 2501") en un punto del mapa, sin usar la ubicación del teléfono.
 *
 *  1. Si es un municipio de la lista ([municipioConocido]), sale al momento y sin internet.
 *  2. Si no, se pregunta a Nominatim, el buscador de OpenStreetMap (el mismo de las calles del
 *     mapa). Solo viaja el texto escrito y la ciudad: sin token, sin correo, sin la ubicación del
 *     teléfono (por eso [ClienteApi.httpSinSesion], que no pone el token de la API). Se pide una
 *     sola vez al tocar "Buscar", como pide su política de uso (nada de buscar letra por letra).
 *
 * También lo usa la administración para poner la ubicación de una actividad, proyecto o centro.
 */
class BuscadorDeLugares(
    private val http: OkHttpClient = ClienteApi.httpSinSesion,
    private val urlBase: String = "https://nominatim.openstreetmap.org/search"
) {

    @Serializable
    private data class Resultado(val lat: String, val lon: String, @SerialName("display_name") val nombre: String = "")

    private val json = Json { ignoreUnknownKeys = true }

    /** El lugar encontrado, o null si no hay nada que se le parezca (o no hay internet). */
    suspend fun buscar(texto: String, ciudad: String): LugarDeReferencia? {
        val limpio = texto.trim().take(MAX_LARGO)
        if (limpio.length < 3) return null
        municipioConocido(limpio)?.let { return it }

        val url = urlBase.toHttpUrl().newBuilder()
            .addQueryParameter("q", "$limpio, $ciudad")
            .addQueryParameter("format", "jsonv2")
            .addQueryParameter("limit", "1")
            .addQueryParameter("countrycodes", "mx")
            .addQueryParameter("accept-language", "es")
            .build()
        val pedido = Request.Builder()
            .url(url)
            // Nominatim pide identificar la app; no lleva nada de la persona.
            .header("User-Agent", "FamiliasQueSuman/3.0 (Android; proyecto escolar Tec de Monterrey)")
            .build()

        return withContext(Dispatchers.IO) {
            runCatching {
                http.newCall(pedido).execute().use { respuesta ->
                    if (!respuesta.isSuccessful) return@use null
                    val cuerpo = respuesta.body?.string() ?: return@use null
                    json.decodeFromString<List<Resultado>>(cuerpo).firstOrNull()?.let { r ->
                        val lat = r.lat.toDoubleOrNull() ?: return@let null
                        val lon = r.lon.toDoubleOrNull() ?: return@let null
                        if (lat !in -90.0..90.0 || lon !in -180.0..180.0) return@let null
                        // "Mitras Centro, Monterrey, Nuevo León, …": basta con las dos primeras partes.
                        val nombre = r.nombre.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            .take(2).joinToString(", ").ifBlank { limpio }
                        LugarDeReferencia(nombre, Coordenada(lat, lon))
                    }
                }
            }.getOrNull()
        }
    }

    private companion object {
        const val MAX_LARGO = 120
    }
}
