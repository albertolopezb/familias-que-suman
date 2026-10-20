package mx.tec.familiasquesuman.data

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.builtins.ListSerializer
import mx.tec.familiasquesuman.data.local.CatalogoLocal
import mx.tec.familiasquesuman.data.red.ApiExcepcion
import mx.tec.familiasquesuman.data.red.CampanaDto
import mx.tec.familiasquesuman.data.red.AportacionCuerpo
import mx.tec.familiasquesuman.data.red.aDominio
import mx.tec.familiasquesuman.data.red.llamar
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Campana
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

/** Lo que pasó al apartar: cuánto quedó apartado del artículo y de la campaña. */
data class Apartado(val articulo: ArticuloMeta, val cantidad: Int, val completadosCampana: Int)

/** Las campañas de donación (la app no cobra: solo registra lo que cada familia se compromete a juntar). */
class CampanaRepository {

    /** Sin conexión salen las guardadas en el teléfono ([CatalogoLocal]); apartar sí pide red. */
    suspend fun getCampanas(): List<Campana> =
        CatalogoLocal.conRespaldo(CLAVE_CAMPANAS, ListSerializer(CampanaDto.serializer())) { llamar { campanas() } }
            .map { it.aDominio() }

    suspend fun getCampana(id: String): Campana = try {
        CatalogoLocal.conRespaldo("campana/$id", CampanaDto.serializer()) { llamar { campana(id) } }.aDominio()
    } catch (e: ApiExcepcion) {
        if (e.sinConexion) {
            CatalogoLocal.leer(CLAVE_CAMPANAS, ListSerializer(CampanaDto.serializer()))
                ?.firstOrNull { it.id == id }?.let { return it.aDominio() }
        }
        if (e.estado == 404 || e.estado == 422) throw NoSuchElementException(e.message) else throw e
    }

    /**
     * Aparta [cantidad] de un artículo. [idempotencia] es el id de ESTE intento: si la red falla y
     * se vuelve a mandar con el mismo id, el servidor no lo cuenta dos veces.
     */
    suspend fun apartar(articuloId: String, cantidad: Int, idempotencia: String = UUID.randomUUID().toString()): Apartado {
        val r = llamar { apartar(articuloId, AportacionCuerpo(idempotencia, cantidad)) }
        return Apartado(ArticuloMeta(r.articulo.id, r.articulo.nombre, r.articulo.meta, r.articulo.apartados), r.cantidad, r.completadosCampana)
    }

    // ── Administración ──

    private fun cuerpo(c: Campana): JsonObject = buildJsonObject {
        put("titulo", c.titulo)
        put("categoria", c.categoria)
        cierreDe(c.cierra)?.let { put("cierra_en", it.toString()) }
        put("urgente", c.urgente)
        put("descripcion", c.descripcion)
        put("unidad_meta", c.unidadMeta)
        put("meta_total", c.metaTotal)
        put("completados", c.completados)
        put("ciudad", c.ciudad.ifBlank { "Monterrey" })
        put("texto_boton", c.textoBoton.ifBlank { "Quiero ayudar" })
        put("telefono", c.telefono.orEmpty())
        put("whatsapp", c.whatsapp.orEmpty())
        put("contacto_nombre", c.contactoNombre.orEmpty())
        put("meta_texto", c.metaTexto.orEmpty())
        put("descripcion_larga", c.descripcionLarga)
        put("como_ayudar", c.comoAyudar)
        put("instagram", c.instagram.orEmpty())
        put("imagen", c.imagen.orEmpty())
        c.asociacionId.takeIf { it.isNotBlank() }?.let { put("asociacion_id", it) }
        c.coordenada?.let { put("latitud", it.latitud); put("longitud", it.longitud) }
    }

    suspend fun agregarCampana(campana: Campana) { llamar { crearCampana(cuerpo(campana)) } }
    suspend fun editarCampana(campana: Campana) { llamar { editarCampana(campana.id, cuerpo(campana)) } }
    suspend fun borrarCampana(id: String) { llamar { borrarCampana(id) }.exigirExito() }

    private companion object {
        const val CLAVE_CAMPANAS = "campanas"

        val DIA_MES: DateTimeFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-MX"))

        /** "30 de diciembre" → la próxima vez que caiga ese día (este año o el que sigue). */
        fun cierreDe(texto: String): LocalDate? {
            if (texto.isBlank()) return null
            val dia = runCatching { MonthDay.parse(texto.trim().lowercase(), DIA_MES) }.getOrNull() ?: return null
            val hoy = LocalDate.now()
            val este = dia.atYear(hoy.year)
            return if (este.isBefore(hoy)) dia.atYear(hoy.year + 1) else este
        }
    }
}
