package mx.tec.familiasquesuman.data.red

import mx.tec.familiasquesuman.domain.EstadoRevision
import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Acompanante
import mx.tec.familiasquesuman.domain.Aportacion
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.domain.CentroVisiteo
import mx.tec.familiasquesuman.domain.Coordenada
import mx.tec.familiasquesuman.domain.EstadoParticipacion
import mx.tec.familiasquesuman.domain.EstadoSugerencia
import mx.tec.familiasquesuman.domain.FormaDeApoyo
import mx.tec.familiasquesuman.domain.IconoApoyo
import mx.tec.familiasquesuman.domain.MomentoEncuesta
import mx.tec.familiasquesuman.domain.OpcionDonacion
import mx.tec.familiasquesuman.domain.Participacion
import mx.tec.familiasquesuman.domain.Proyecto
import mx.tec.familiasquesuman.domain.PuntoEntrega
import mx.tec.familiasquesuman.domain.RespuestaEncuesta
import mx.tec.familiasquesuman.domain.RespuestaPregunta
import mx.tec.familiasquesuman.domain.Sexo
import mx.tec.familiasquesuman.domain.Sugerencia
import mx.tec.familiasquesuman.domain.TemaActividad
import mx.tec.familiasquesuman.domain.Testimonio
import mx.tec.familiasquesuman.domain.TipoSugerencia
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// De lo que manda la API (Dto.kt) a lo que usan las pantallas (domain/). Aquí, y solo aquí, se
// decide cómo se escribe una fecha: así ninguna pantalla cambió al llegar el backend.

private val ES = Locale.forLanguageTag("es-MX")
private val DIA_LARGO = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", ES)     // sábado, 3 de octubre
private val DIA_MES = DateTimeFormatter.ofPattern("d 'de' MMMM", ES)             // 30 de diciembre
private val CORTA = DateTimeFormatter.ofPattern("d MMM yyyy", ES)                // 3 ago 2026
private val MES = DateTimeFormatter.ofPattern("MMMM", ES)
private val HORA = DateTimeFormatter.ofPattern("HH:mm")

/** La hora de la CIUDAD de la actividad: la API la manda con su zona ("…-06:00"). */
fun horaLocal(iso: String): LocalDateTime = OffsetDateTime.parse(iso).toLocalDateTime()

fun fechaLarga(momento: LocalDateTime): String = momento.format(DIA_LARGO)
fun fechaCortaDe(iso: String): String = LocalDate.parse(iso.take(10)).format(CORTA)

fun horario(inicio: LocalDateTime, fin: LocalDateTime?): String =
    if (fin == null) inicio.format(HORA) else "${inicio.format(HORA)} – ${fin.format(HORA)}"

private fun coordenada(lat: Double?, lng: Double?) = if (lat != null && lng != null) Coordenada(lat, lng) else null

fun AsociacionDto.aDominio() = Asociacion(id, nombre, categoria, descripcion, direccion, telefono, whatsapp, correo)

fun ActividadDto.aDominio(): Actividad {
    val inicio = horaLocal(iniciaEn)
    val fin = terminaEn?.let(::horaLocal)
    return Actividad(
        id = id,
        titulo = titulo,
        asociacionId = asociacion.id,
        fecha = fechaLarga(inicio),
        horario = horario(inicio, fin),
        direccion = direccion,
        edadMinima = edadMinima,
        descripcion = descripcion,
        cupoTotal = cupoTotal,
        lugaresDisponibles = lugaresDisponibles,
        municipio = municipio,
        tema = runCatching { TemaActividad.valueOf(tema) }.getOrDefault(TemaActividad.GENERAL),
        aportacion = when (aportacion.tipo) {
            "especie" -> Aportacion.EnEspecie(aportacion.detalle)
            "monetaria" -> Aportacion.Monetaria(aportacion.monto, aportacion.detalle)
            else -> Aportacion.Ninguna
        },
        puntoDeEncuentro = puntoEncuentro,
        acercaDelProyecto = acercaDelProyecto,
        queHaremos = queHaremos,
        queIncluye = queIncluye,
        queLlevar = queLlevar,
        recomendaciones = recomendaciones,
        foto = imagen,
        yaPaso = yaPaso,
        ciudad = ciudad,
        inicio = inicio,
        fin = fin,
        coordenada = coordenada(latitud, longitud)
    )
}

fun CampanaDto.aDominio() = Campana(
    id = id,
    titulo = titulo,
    asociacionId = asociacionId.orEmpty(),
    categoria = categoria,
    cierra = cierraEn?.let { LocalDate.parse(it).format(DIA_MES) }.orEmpty(),
    urgente = urgente,
    descripcion = descripcion,
    unidadMeta = unidadMeta,
    metaTotal = metaTotal,
    completados = completados,
    articulos = articulos.map { ArticuloMeta(it.id, it.nombre, it.meta, it.apartados) },
    imagen = imagen,
    ciudad = ciudad,
    textoBoton = textoBoton,
    telefono = telefono,
    whatsapp = whatsapp,
    contactoNombre = contactoNombre,
    metaTexto = metaTexto,
    descripcionLarga = descripcionLarga,
    comoAyudar = comoAyudar,
    opcionesDonacion = opcionesDonacion.map { OpcionDonacion(it.nombre, it.precio) },
    puntosEntrega = puntosEntrega.map { PuntoEntrega(it.direccion, it.colonia) },
    instagram = instagram,
    coordenada = coordenada(latitud, longitud)
)

fun ProyectoDto.aDominio() = Proyecto(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    beneficiarios = beneficiarios,
    ciudad = ciudad,
    vigencia = vigencia,
    logo = logo,
    activo = activo,
    resumen = resumen,
    acercaDe = acercaDe,
    formasDeApoyo = formasApoyo.map {
        FormaDeApoyo(it.titulo, it.detalle, runCatching { IconoApoyo.valueOf(it.icono) }.getOrDefault(IconoApoyo.CORAZON))
    },
    comoAyudar = comoAyudar,
    telefono = telefono,
    whatsapp = whatsapp,
    instagram = instagram,
    coordenada = coordenada(latitud, longitud)
)

fun CentroDto.aDominio() = CentroVisiteo(
    id = id,
    nombre = nombre,
    tipo = tipo,
    resumen = resumen,
    informacion = informacion,
    necesidades = necesidades,
    direccion = direccion,
    logo = logo,
    verificado = verificado,
    comoAyudar = comoAyudar,
    recomendaciones = recomendaciones,
    telefono = telefono,
    whatsapp = whatsapp,
    instagram = instagram
)

/** [foto] es la ruta del archivo ya descargado en el teléfono (o null). */
fun TestimonioDto.aDominio(foto: String?, correoPropio: String?) = Testimonio(
    id = id,
    participacionId = participacionId,
    actividadTitulo = actividadTitulo,
    familia = familia,
    // El servidor nunca manda correos ajenos: si es mío, se pone el mío; si no, va vacío.
    correo = if (esMio) correoPropio.orEmpty() else "",
    experiencia = experiencia,
    foto = foto,
    fecha = fechaCortaDe(fecha),
    esMio = esMio,
    estado = when (estado) {
        "pendiente" -> EstadoRevision.PENDIENTE
        "rechazado" -> EstadoRevision.RECHAZADO
        else -> EstadoRevision.APROBADO
    }
)

fun ParticipacionDto.aDominio(): Participacion {
    val inicio = horaLocal(iniciaEn)
    return Participacion(
        id = id,
        tituloActividad = tituloActividad,
        nombreAsociacion = nombreAsociacion,
        fecha = fechaLarga(inicio),
        mes = inicio.format(MES).uppercase(ES),
        estado = when {
            testimonioId != null -> EstadoParticipacion.TESTIMONIO_PUBLICADO
            encuestaPendiente -> EstadoParticipacion.ENCUESTA_PENDIENTE
            else -> EstadoParticipacion.SIN_PENDIENTES
        }
    )
}

/** En la app, la encuesta de después se identifica por la PARTICIPACIÓN; la de antes, por la actividad. */
fun EncuestaDto.aDominio(correoPorOmision: String) = RespuestaEncuesta(
    id = id,
    actividadId = if (momento == "despues") participacionId ?: actividadId else actividadId,
    actividadTitulo = actividadTitulo,
    correo = correo ?: correoPorOmision,
    familia = familia,
    momento = if (momento == "despues") MomentoEncuesta.DESPUES else MomentoEncuesta.ANTES,
    respuestas = respuestas.map { RespuestaPregunta(it.numero, it.pregunta, it.respuesta) },
    fecha = fechaCortaDe(fecha)
)

fun SugerenciaDto.aDominio() = Sugerencia(
    id = id,
    tipo = runCatching { TipoSugerencia.valueOf(tipo) }.getOrDefault(TipoSugerencia.ACTIVIDAD),
    nombre = nombre,
    descripcion = descripcion,
    detalleExtra = detalleExtra,
    ciudad = ciudad,
    organizacion = organizacion,
    contactoCausa = contactoCausa,
    sugeridaPor = sugeridaPor,
    contactoDeQuienSugiere = contacto,
    fecha = fechaCortaDe(fecha),
    estado = runCatching { EstadoSugerencia.valueOf(estado) }.getOrDefault(EstadoSugerencia.PENDIENTE)
)

fun PersonaDto.aAcompanante() = Acompanante(
    nombre = nombre,
    edad = edad ?: 0,
    fechaNacimiento = fechaNacimiento?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    sexo = sexo?.let { runCatching { Sexo.valueOf(it) }.getOrNull() }
)

fun AcompananteDto.aAcompanante() = PersonaDto(nombre, fechaNacimiento, edad, sexo).aAcompanante()

fun Acompanante.aPersona() = PersonaDto(
    nombre = nombre.trim(),
    fechaNacimiento = fechaNacimiento?.toString(),
    edad = if (fechaNacimiento == null) edad else null,
    sexo = sexo?.name
)
