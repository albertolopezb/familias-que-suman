package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Coordenada
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.builtins.ListSerializer
import mx.tec.familiasquesuman.data.local.CatalogoLocal
import mx.tec.familiasquesuman.data.red.ActividadDto
import mx.tec.familiasquesuman.data.red.ApiExcepcion
import mx.tec.familiasquesuman.data.red.AsociacionDto
import mx.tec.familiasquesuman.data.red.CentroDto
import mx.tec.familiasquesuman.data.red.ProyectoDto
import mx.tec.familiasquesuman.data.red.AsociacionCuerpo
import mx.tec.familiasquesuman.data.red.CiudadDto
import mx.tec.familiasquesuman.data.red.ClienteApi
import mx.tec.familiasquesuman.data.red.ErrorDto
import mx.tec.familiasquesuman.data.red.InscritoDto
import mx.tec.familiasquesuman.data.red.aDominio
import mx.tec.familiasquesuman.data.red.llamar
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.ActividadConAsociacion
import mx.tec.familiasquesuman.domain.Aportacion
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.CentroVisiteo
import mx.tec.familiasquesuman.domain.Proyecto
import mx.tec.familiasquesuman.domain.fechaDeTexto
import mx.tec.familiasquesuman.domain.horaDeFin
import mx.tec.familiasquesuman.domain.horaDeInicio
import java.time.LocalTime
import java.time.ZoneId

/**
 * Actividades, asociaciones, proyectos y centros: todo lo de la API (backend/api).
 *
 * Lo que se lee es público; lo de administración manda el token y el SERVIDOR decide si se puede
 * (la app solo esconde los botones). Cualquier falla llega como [ApiExcepcion].
 *
 * Lo público se guarda en el teléfono ([CatalogoLocal]): sin conexión, las listas y las fichas
 * siguen saliendo con lo último que llegó.
 */
class ActividadRepository {

    /** La última lista que llegó: para encontrar una actividad por id sin volver a pedirla. */
    @Volatile private var ultimas: List<ActividadConAsociacion> = emptyList()
    @Volatile private var ciudadesGuardadas: List<CiudadDto> = emptyList()

    suspend fun getActividadesConAsociacion(): List<ActividadConAsociacion> =
        CatalogoLocal.conRespaldo(CLAVE_ACTIVIDADES, ListSerializer(ActividadDto.serializer())) { llamar { actividades() } }
            .map { ActividadConAsociacion(it.aDominio(), it.asociacion.aDominio()) }.also { ultimas = it }

    suspend fun getActividades(): List<Actividad> = getActividadesConAsociacion().map { it.actividad }

    suspend fun getActividadConAsociacion(id: String): ActividadConAsociacion = try {
        val dto = CatalogoLocal.conRespaldo("actividad/$id", ActividadDto.serializer()) { llamar { actividad(id) } }
        ActividadConAsociacion(dto.aDominio(), dto.asociacion.aDominio())
    } catch (e: ApiExcepcion) {
        // Sin conexión y sin esa ficha guardada: puede venir en la última lista guardada.
        if (e.sinServidor) {
            CatalogoLocal.leer(CLAVE_ACTIVIDADES, ListSerializer(ActividadDto.serializer()))
                ?.firstOrNull { it.id == id }
                ?.let { return ActividadConAsociacion(it.aDominio(), it.asociacion.aDominio()) }
        }
        // Las pantallas ya distinguen "no existe" de "falló la red".
        if (e.estado == 404 || e.estado == 422) throw NoSuchElementException(e.message) else throw e
    }

    suspend fun getActividad(id: String): Actividad = getActividadConAsociacion(id).actividad

    /** Lo último que se cargó de esta actividad (sin red); null si no se ha visto. */
    fun enMemoria(id: String): ActividadConAsociacion? = ultimas.firstOrNull { it.actividad.id == id }

    /** Las del directorio de Explorar. */
    suspend fun getAsociaciones(): List<Asociacion> =
        CatalogoLocal.conRespaldo("asociaciones", ListSerializer(AsociacionDto.serializer())) { llamar { asociaciones() } }
            .map { it.aDominio() }

    /** También las que solo organizan actividades (Familias que Suman, Regalando Estrellas…). */
    suspend fun getTodasLasAsociaciones(): List<Asociacion> =
        CatalogoLocal.conRespaldo(CLAVE_TODAS, ListSerializer(AsociacionDto.serializer())) { llamar { asociaciones(todas = true) } }
            .map { it.aDominio() }

    suspend fun getAsociacion(id: String): Asociacion = try {
        CatalogoLocal.conRespaldo("asociacion/$id", AsociacionDto.serializer()) { llamar { asociacion(id) } }.aDominio()
    } catch (e: ApiExcepcion) {
        if (!e.sinServidor) throw e
        CatalogoLocal.leer(CLAVE_TODAS, ListSerializer(AsociacionDto.serializer()))
            ?.firstOrNull { it.id == id }?.aDominio() ?: throw e
    }

    suspend fun getProyectos(): List<Proyecto> =
        CatalogoLocal.conRespaldo("proyectos", ListSerializer(ProyectoDto.serializer())) { llamar { proyectos() } }
            .map { it.aDominio() }

    suspend fun getCentros(): List<CentroVisiteo> =
        CatalogoLocal.conRespaldo("centros", ListSerializer(CentroDto.serializer())) { llamar { centros() } }
            .map { it.aDominio() }

    suspend fun getCiudades(): List<CiudadDto> = ciudadesGuardadas.ifEmpty {
        CatalogoLocal.conRespaldo("ciudades", ListSerializer(CiudadDto.serializer())) { llamar { ciudades() } }
            .also { ciudadesGuardadas = it }
    }

    /** El enlace para invitar a otra familia: abre la app (o una página con "Abrir en la app"). */
    suspend fun enlaceDe(actividadId: String): String = llamar { enlace(actividadId) }.url

    // ── Administración ──

    private fun cuerpoAsociacion(a: Asociacion) = AsociacionCuerpo(
        nombre = a.nombre, categoria = a.categoria, descripcion = a.descripcion, direccion = a.direccion,
        telefono = a.telefono, whatsapp = a.whatsapp, correo = a.correo
    )

    suspend fun agregarAsociacion(asociacion: Asociacion) { llamar { crearAsociacion(cuerpoAsociacion(asociacion)) } }
    suspend fun editarAsociacion(asociacion: Asociacion) { llamar { editarAsociacion(asociacion.id, cuerpoAsociacion(asociacion)) } }
    suspend fun borrarAsociacion(id: String) { llamar { borrarAsociacion(id) }.exigirExito() }

    /**
     * El formulario de administración escribe la fecha como en la app ("sábado, 3 de octubre") y
     * el horario como "09:45 – 11:30"; la API pide fecha y hora con zona. Se traduce aquí.
     */
    suspend fun guardarActividad(
        id: String?,
        titulo: String,
        descripcion: String,
        fecha: String,
        horario: String,
        direccion: String,
        cupoTotal: Int,
        lugaresDisponibles: Int,
        edadMinima: Int?,
        ciudad: String,
        asociacionId: String?,
        coordenada: Coordenada?
    ) {
        val zona = ZoneId.of(getCiudades().firstOrNull { it.nombre == ciudad }?.zonaHoraria ?: "America/Monterrey")
        val dia = fechaDeTexto(fecha) ?: throw ApiExcepcion(422, "fecha_invalida", "Escribe la fecha como \"sábado, 3 de octubre\".")
        val inicio = dia.atTime(horaDeInicio(horario) ?: LocalTime.of(9, 0)).atZone(zona)
        val fin = horaDeFin(horario)?.let { dia.atTime(it).atZone(zona) }
        val organizador = asociacionId
            ?: enMemoria(id.orEmpty())?.asociacion?.id
            ?: getTodasLasAsociaciones().let { todas -> (todas.firstOrNull { it.nombre == "Familias que Suman" } ?: todas.first()).id }
        val cuerpo: JsonObject = buildJsonObject {
            put("titulo", titulo)
            put("asociacion_id", organizador)
            put("descripcion", descripcion)
            put("inicia_en", inicio.toOffsetDateTime().toString())
            fin?.let { put("termina_en", it.toOffsetDateTime().toString()) }
            put("direccion", direccion)
            put("cupo_total", cupoTotal)
            put("lugares_disponibles", lugaresDisponibles.coerceIn(0, cupoTotal))
            edadMinima?.let { put("edad_minima", it) }
            put("ciudad", ciudad)
            // PUT reemplaza todo: lo que el formulario no edita se manda como ya estaba.
            enMemoria(id.orEmpty())?.actividad?.let { base ->
                put("municipio", base.municipio)
                put("tema", base.tema.name)
                when (val a = base.aportacion) {
                    is Aportacion.EnEspecie -> { put("aportacion_tipo", "especie"); put("aportacion_detalle", a.detalle) }
                    is Aportacion.Monetaria -> { put("aportacion_tipo", "monetaria"); put("aportacion_monto", a.monto); put("aportacion_detalle", a.detalle) }
                    Aportacion.Ninguna -> put("aportacion_tipo", "ninguna")
                }
                put("punto_encuentro", base.puntoDeEncuentro)
                put("acerca_del_proyecto", base.acercaDelProyecto)
                put("que_haremos", base.queHaremos)
                put("que_incluye", base.queIncluye)
                put("que_llevar", base.queLlevar)
                put("recomendaciones", base.recomendaciones)
                put("imagen", base.foto.orEmpty())
            }
            // Dónde sale en el mapa: lo que escribió la administración (vacío = no sale).
            coordenada?.let { put("latitud", it.latitud); put("longitud", it.longitud) }
        }
        if (id == null) llamar { crearActividad(cuerpo) } else llamar { editarActividad(id, cuerpo) }
    }

    /** RF-06: quiénes van (lo ve quien registra asistencias; el servidor lo revisa y lo audita). */
    suspend fun inscritos(actividadId: String): List<InscritoDto> = llamar { inscritos(actividadId) }

    suspend fun duplicarActividad(id: String) { llamar { duplicarActividad(id) } }
    suspend fun borrarActividad(id: String) { llamar { borrarActividad(id) }.exigirExito() }

    private fun cuerpoProyecto(p: Proyecto): JsonObject = buildJsonObject {
        put("nombre", p.nombre)
        put("descripcion", p.descripcion)
        put("beneficiarios", p.beneficiarios.orEmpty())
        put("ciudad", p.ciudad.ifBlank { "Monterrey" })
        put("vigencia", p.vigencia.orEmpty())
        put("activo", p.activo)
        put("resumen", p.resumen)
        put("acerca_de", p.acercaDe)
        put("como_ayudar", p.comoAyudar)
        put("telefono", p.telefono.orEmpty())
        put("whatsapp", p.whatsapp.orEmpty())
        put("instagram", p.instagram.orEmpty())
        put("logo", p.logo.orEmpty())
        p.coordenada?.let { put("latitud", it.latitud); put("longitud", it.longitud) }
    }

    suspend fun agregarProyecto(proyecto: Proyecto) { llamar { crearProyecto(cuerpoProyecto(proyecto)) } }
    suspend fun editarProyecto(proyecto: Proyecto) { llamar { editarProyecto(proyecto.id, cuerpoProyecto(proyecto)) } }
    suspend fun borrarProyecto(id: String) { llamar { borrarProyecto(id) }.exigirExito() }

    private fun cuerpoCentro(c: CentroVisiteo): JsonObject = buildJsonObject {
        put("nombre", c.nombre)
        put("tipo", c.tipo.ifBlank { "Casas hogar" })
        put("resumen", c.resumen)
        put("informacion", c.informacion)
        put("necesidades", JsonArray(c.necesidades.map { JsonPrimitive(it) }))
        put("direccion", c.direccion)
        put("verificado", c.verificado)
        put("como_ayudar", c.comoAyudar)
        put("recomendaciones", c.recomendaciones)
        put("telefono", c.telefono.orEmpty())
        put("whatsapp", c.whatsapp.orEmpty())
        put("instagram", c.instagram.orEmpty())
        put("logo", c.logo.orEmpty())
    }

    suspend fun agregarCentro(centro: CentroVisiteo) { llamar { crearCentro(cuerpoCentro(centro)) } }
    suspend fun editarCentro(centro: CentroVisiteo) { llamar { editarCentro(centro.id, cuerpoCentro(centro)) } }
    suspend fun borrarCentro(id: String) { llamar { borrarCentro(id) }.exigirExito() }

    private companion object {
        const val CLAVE_ACTIVIDADES = "actividades"
        const val CLAVE_TODAS = "asociaciones/todas"
    }
}

/** Un DELETE que no respondió 2xx se convierte en error, como cualquier otra llamada. */
internal fun retrofit2.Response<Unit>.exigirExito() {
    if (isSuccessful) return
    val error = errorBody()?.string()?.let {
        runCatching { ClienteApi.json.decodeFromString(ErrorDto.serializer(), it) }.getOrNull()
    }
    throw ApiExcepcion(code(), error?.codigo ?: "http_${code()}", error?.error ?: "No se pudo completar. Intenta de nuevo.", error?.datos)
}
