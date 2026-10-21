"""Las tablas de PostgreSQL vistas como clases de Python (el ORM).

Las tablas las crean y cambian las migraciones de Flyway (`backend/db/migrations`), NO este
archivo: aquí solo se describen para poder consultarlas. Si una columna cambia, primero va la
migración y después se refleja aquí.

Todos los id son UUID v4 que genera PostgreSQL (`gen_random_uuid()`, con el generador
criptográfico del sistema). No se pueden adivinar ni recorrer como 1, 2, 3…: eso le quita
la mitad del trabajo a un ataque IDOR/BOLA (OWASP API1). La otra mitad es revisar SIEMPRE de
quién es cada cosa (ver politicas.py).
"""

import uuid
from datetime import date, datetime

from sqlalchemy import ARRAY, BigInteger, Boolean, Date, DateTime, Float, ForeignKey, Identity, Integer, Text, text
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from .db import Base


def _id() -> Mapped[uuid.UUID]:
    return mapped_column(UUID(as_uuid=True), primary_key=True, server_default=text("gen_random_uuid()"))


class Ciudad(Base):
    __tablename__ = "ciudades"
    nombre: Mapped[str] = mapped_column(Text, primary_key=True)
    estado: Mapped[str] = mapped_column(Text)
    zona_horaria: Mapped[str] = mapped_column(Text)
    latitud: Mapped[float] = mapped_column(Float)
    longitud: Mapped[float] = mapped_column(Float)


class Cuenta(Base):
    __tablename__ = "cuentas"
    id: Mapped[uuid.UUID] = _id()
    correo: Mapped[str] = mapped_column(Text)
    # Solo el hash Argon2id (ver identidad.py). Nunca sale en una respuesta (ver esquemas.YoSalida).
    contrasena_hash: Mapped[str | None] = mapped_column(Text)
    correo_verificado: Mapped[bool] = mapped_column(Boolean, server_default=text("false"))
    rol: Mapped[str] = mapped_column(Text, server_default=text("'familia'"))
    permisos: Mapped[list[str]] = mapped_column(ARRAY(Text), server_default=text("'{}'"))
    # Un token emitido ANTES de esta fecha ya no sirve (cambio de contraseña o de permisos).
    tokens_validos_desde: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))
    nombre_familia: Mapped[str] = mapped_column(Text)
    ciudad: Mapped[str] = mapped_column(Text, server_default=text("'Monterrey'"))
    foto_clave: Mapped[str | None] = mapped_column(Text)
    titular_nombre: Mapped[str | None] = mapped_column(Text)
    titular_nacimiento: Mapped[date | None] = mapped_column(Date)
    titular_sexo: Mapped[str | None] = mapped_column(Text)
    aviso_aceptado_en: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    ultimo_acceso_en: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    creada_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))


class Asociacion(Base):
    __tablename__ = "asociaciones"
    id: Mapped[uuid.UUID] = _id()
    nombre: Mapped[str] = mapped_column(Text)
    categoria: Mapped[str] = mapped_column(Text)
    descripcion: Mapped[str] = mapped_column(Text, server_default=text("''"))
    ciudad: Mapped[str] = mapped_column(Text, server_default=text("'Monterrey'"))
    direccion: Mapped[str] = mapped_column(Text, server_default=text("''"))
    telefono: Mapped[str] = mapped_column(Text, server_default=text("''"))
    whatsapp: Mapped[str] = mapped_column(Text, server_default=text("''"))
    correo: Mapped[str] = mapped_column(Text, server_default=text("''"))
    en_directorio: Mapped[bool] = mapped_column(Boolean, server_default=text("true"))


class Actividad(Base):
    __tablename__ = "actividades"
    id: Mapped[uuid.UUID] = _id()
    asociacion_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("asociaciones.id"))
    titulo: Mapped[str] = mapped_column(Text)
    descripcion: Mapped[str] = mapped_column(Text, server_default=text("''"))
    inicia_en: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    termina_en: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    direccion: Mapped[str] = mapped_column(Text, server_default=text("''"))
    cupo_total: Mapped[int] = mapped_column(Integer, server_default=text("0"))
    ocupados_fuera: Mapped[int] = mapped_column(Integer, server_default=text("0"))
    ciudad: Mapped[str] = mapped_column(Text, server_default=text("'Monterrey'"))
    municipio: Mapped[str] = mapped_column(Text, server_default=text("''"))
    edad_minima: Mapped[int | None] = mapped_column(Integer)
    tema: Mapped[str] = mapped_column(Text, server_default=text("'GENERAL'"))
    aportacion_tipo: Mapped[str] = mapped_column(Text, server_default=text("'ninguna'"))
    aportacion_monto: Mapped[str] = mapped_column(Text, server_default=text("''"))
    aportacion_detalle: Mapped[str] = mapped_column(Text, server_default=text("''"))
    punto_encuentro: Mapped[str] = mapped_column(Text, server_default=text("''"))
    acerca_del_proyecto: Mapped[str] = mapped_column(Text, server_default=text("''"))
    que_haremos: Mapped[str] = mapped_column(Text, server_default=text("''"))
    que_incluye: Mapped[str] = mapped_column(Text, server_default=text("''"))
    que_llevar: Mapped[str] = mapped_column(Text, server_default=text("''"))
    recomendaciones: Mapped[str] = mapped_column(Text, server_default=text("''"))
    imagen: Mapped[str | None] = mapped_column(Text)
    latitud: Mapped[float | None] = mapped_column(Float)
    longitud: Mapped[float | None] = mapped_column(Float)
    creada_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))

    asociacion: Mapped[Asociacion] = relationship(lazy="joined")


class Inscripcion(Base):
    __tablename__ = "inscripciones"
    id: Mapped[uuid.UUID] = _id()
    cuenta_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("cuentas.id", ondelete="CASCADE"))
    actividad_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("actividades.id"))
    personas: Mapped[int] = mapped_column(Integer)
    creada_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))
    actualizada_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))
    cancelada_en: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    consentimiento_menores_en: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))

    integrantes: Mapped[list["InscripcionPersona"]] = relationship(
        order_by="InscripcionPersona.orden", cascade="all, delete-orphan", lazy="selectin"
    )


class InscripcionPersona(Base):
    __tablename__ = "inscripcion_personas"
    id: Mapped[uuid.UUID] = _id()
    inscripcion_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("inscripciones.id", ondelete="CASCADE"))
    nombre: Mapped[str] = mapped_column(Text)
    fecha_nacimiento: Mapped[date | None] = mapped_column(Date)
    edad: Mapped[int | None] = mapped_column(Integer)
    sexo: Mapped[str | None] = mapped_column(Text)
    orden: Mapped[int] = mapped_column(Integer, server_default=text("0"))


class Acompanante(Base):
    __tablename__ = "acompanantes"
    id: Mapped[uuid.UUID] = _id()
    cuenta_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("cuentas.id", ondelete="CASCADE"))
    nombre: Mapped[str] = mapped_column(Text)
    fecha_nacimiento: Mapped[date | None] = mapped_column(Date)
    edad: Mapped[int | None] = mapped_column(Integer)
    sexo: Mapped[str | None] = mapped_column(Text)
    creado_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))


class Favorito(Base):
    __tablename__ = "favoritos"
    cuenta_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("cuentas.id", ondelete="CASCADE"), primary_key=True)
    tipo: Mapped[str] = mapped_column(Text, primary_key=True)
    objeto_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), primary_key=True)
    creado_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))


class Preferencias(Base):
    __tablename__ = "preferencias"
    cuenta_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("cuentas.id", ondelete="CASCADE"), primary_key=True)
    recordatorio_actividades: Mapped[bool] = mapped_column(Boolean, server_default=text("true"))
    avisos_favoritos: Mapped[bool] = mapped_column(Boolean, server_default=text("true"))
    urgencias_ciudad: Mapped[bool] = mapped_column(Boolean, server_default=text("false"))


class Asistencia(Base):
    __tablename__ = "asistencias"
    id: Mapped[uuid.UUID] = _id()
    cuenta_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("cuentas.id", ondelete="CASCADE"))
    actividad_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("actividades.id"))
    registrada_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))

    actividad: Mapped[Actividad] = relationship(lazy="joined")


class Testimonio(Base):
    __tablename__ = "testimonios"
    id: Mapped[uuid.UUID] = _id()
    asistencia_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("asistencias.id", ondelete="CASCADE"), unique=True)
    experiencia: Mapped[str] = mapped_column(Text)
    foto_clave: Mapped[str | None] = mapped_column(Text)
    creado_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))
    # 'pendiente' hasta que la moderación lo revisa; solo lo 'aprobado' es público (RF-12).
    estado: Mapped[str] = mapped_column(Text, server_default=text("'pendiente'"))
    revisado_en: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    revisado_por: Mapped[uuid.UUID | None] = mapped_column(UUID(as_uuid=True))

    asistencia: Mapped[Asistencia] = relationship(lazy="joined")


class RespuestaEncuesta(Base):
    __tablename__ = "respuestas_encuesta"
    id: Mapped[uuid.UUID] = _id()
    cuenta_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("cuentas.id", ondelete="CASCADE"))
    actividad_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("actividades.id"))
    asistencia_id: Mapped[uuid.UUID | None] = mapped_column(ForeignKey("asistencias.id", ondelete="CASCADE"))
    momento: Mapped[str] = mapped_column(Text)
    respuestas: Mapped[list] = mapped_column(JSONB)
    creada_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))

    actividad: Mapped[Actividad] = relationship(lazy="joined")


class Invitacion(Base):
    __tablename__ = "invitaciones"
    id: Mapped[uuid.UUID] = _id()
    cuenta_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("cuentas.id", ondelete="CASCADE"))
    actividad_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("actividades.id"))
    creada_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))


class Proyecto(Base):
    __tablename__ = "proyectos"
    id: Mapped[uuid.UUID] = _id()
    nombre: Mapped[str] = mapped_column(Text)
    resumen: Mapped[str] = mapped_column(Text, server_default=text("''"))
    descripcion: Mapped[str] = mapped_column(Text, server_default=text("''"))
    beneficiarios: Mapped[str | None] = mapped_column(Text)
    ciudad: Mapped[str] = mapped_column(Text, server_default=text("'Monterrey'"))
    activo: Mapped[bool] = mapped_column(Boolean, server_default=text("true"))
    telefono: Mapped[str | None] = mapped_column(Text)
    whatsapp: Mapped[str | None] = mapped_column(Text)
    logo_clave: Mapped[str | None] = mapped_column(Text)
    vigencia: Mapped[str | None] = mapped_column(Text)
    acerca_de: Mapped[str] = mapped_column(Text, server_default=text("''"))
    como_ayudar: Mapped[str] = mapped_column(Text, server_default=text("''"))
    instagram: Mapped[str | None] = mapped_column(Text)
    latitud: Mapped[float | None] = mapped_column(Float)
    longitud: Mapped[float | None] = mapped_column(Float)

    formas_apoyo: Mapped[list["FormaApoyo"]] = relationship(
        order_by="FormaApoyo.orden", cascade="all, delete-orphan", lazy="selectin"
    )


class FormaApoyo(Base):
    __tablename__ = "formas_apoyo"
    id: Mapped[uuid.UUID] = _id()
    proyecto_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("proyectos.id", ondelete="CASCADE"))
    titulo: Mapped[str] = mapped_column(Text)
    detalle: Mapped[str] = mapped_column(Text, server_default=text("''"))
    icono: Mapped[str] = mapped_column(Text, server_default=text("'CORAZON'"))
    orden: Mapped[int] = mapped_column(Integer, server_default=text("0"))


class Centro(Base):
    __tablename__ = "centros"
    id: Mapped[uuid.UUID] = _id()
    nombre: Mapped[str] = mapped_column(Text)
    tipo: Mapped[str] = mapped_column(Text)
    resumen: Mapped[str] = mapped_column(Text, server_default=text("''"))
    necesidades: Mapped[list[str]] = mapped_column(ARRAY(Text), server_default=text("'{}'"))
    direccion: Mapped[str] = mapped_column(Text, server_default=text("''"))
    verificado: Mapped[bool] = mapped_column(Boolean, server_default=text("false"))
    telefono: Mapped[str | None] = mapped_column(Text)
    whatsapp: Mapped[str | None] = mapped_column(Text)
    logo_clave: Mapped[str | None] = mapped_column(Text)
    informacion: Mapped[str] = mapped_column(Text, server_default=text("''"))
    como_ayudar: Mapped[str] = mapped_column(Text, server_default=text("''"))
    recomendaciones: Mapped[str] = mapped_column(Text, server_default=text("''"))
    instagram: Mapped[str | None] = mapped_column(Text)
    ciudad: Mapped[str] = mapped_column(Text, server_default=text("'Monterrey'"))
    latitud: Mapped[float | None] = mapped_column(Float)
    longitud: Mapped[float | None] = mapped_column(Float)


class Campana(Base):
    __tablename__ = "campanas"
    id: Mapped[uuid.UUID] = _id()
    asociacion_id: Mapped[uuid.UUID | None] = mapped_column(ForeignKey("asociaciones.id"))
    titulo: Mapped[str] = mapped_column(Text)
    descripcion: Mapped[str] = mapped_column(Text, server_default=text("''"))
    categoria: Mapped[str] = mapped_column(Text, server_default=text("''"))
    cierra_en: Mapped[date | None] = mapped_column(Date)
    urgente: Mapped[bool] = mapped_column(Boolean, server_default=text("false"))
    whatsapp: Mapped[str | None] = mapped_column(Text)
    imagen_clave: Mapped[str | None] = mapped_column(Text)
    ciudad: Mapped[str] = mapped_column(Text, server_default=text("'Monterrey'"))
    texto_boton: Mapped[str] = mapped_column(Text, server_default=text("'Quiero ayudar'"))
    telefono: Mapped[str | None] = mapped_column(Text)
    contacto_nombre: Mapped[str | None] = mapped_column(Text)
    meta_texto: Mapped[str | None] = mapped_column(Text)
    descripcion_larga: Mapped[str] = mapped_column(Text, server_default=text("''"))
    como_ayudar: Mapped[str] = mapped_column(Text, server_default=text("''"))
    instagram: Mapped[str | None] = mapped_column(Text)
    unidad_meta: Mapped[str] = mapped_column(Text, server_default=text("''"))
    meta_total: Mapped[int] = mapped_column(Integer, server_default=text("0"))
    completados_fuera: Mapped[int] = mapped_column(Integer, server_default=text("0"))
    latitud: Mapped[float | None] = mapped_column(Float)
    longitud: Mapped[float | None] = mapped_column(Float)

    articulos: Mapped[list["ArticuloMeta"]] = relationship(cascade="all, delete-orphan", lazy="selectin")
    opciones: Mapped[list["OpcionDonacion"]] = relationship(
        order_by="OpcionDonacion.orden", cascade="all, delete-orphan", lazy="selectin"
    )
    puntos: Mapped[list["PuntoEntrega"]] = relationship(
        order_by="PuntoEntrega.orden", cascade="all, delete-orphan", lazy="selectin"
    )


class ArticuloMeta(Base):
    __tablename__ = "articulos_meta"
    id: Mapped[uuid.UUID] = _id()
    campana_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("campanas.id", ondelete="CASCADE"))
    nombre: Mapped[str] = mapped_column(Text)
    meta: Mapped[int] = mapped_column(Integer)
    apartados_fuera: Mapped[int] = mapped_column(Integer, server_default=text("0"))


class OpcionDonacion(Base):
    __tablename__ = "opciones_donacion"
    id: Mapped[uuid.UUID] = _id()
    campana_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("campanas.id", ondelete="CASCADE"))
    nombre: Mapped[str] = mapped_column(Text)
    precio: Mapped[str] = mapped_column(Text)
    orden: Mapped[int] = mapped_column(Integer, server_default=text("0"))


class PuntoEntrega(Base):
    __tablename__ = "puntos_entrega"
    id: Mapped[uuid.UUID] = _id()
    campana_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("campanas.id", ondelete="CASCADE"))
    direccion: Mapped[str] = mapped_column(Text)
    colonia: Mapped[str] = mapped_column(Text, server_default=text("''"))
    orden: Mapped[int] = mapped_column(Integer, server_default=text("0"))


class Aportacion(Base):
    __tablename__ = "aportaciones"
    id: Mapped[uuid.UUID] = _id()
    cuenta_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("cuentas.id", ondelete="CASCADE"))
    articulo_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("articulos_meta.id", ondelete="CASCADE"))
    cantidad: Mapped[int] = mapped_column(Integer)
    creada_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))


class Sugerencia(Base):
    __tablename__ = "sugerencias"
    id: Mapped[uuid.UUID] = _id()
    tipo: Mapped[str] = mapped_column(Text)
    nombre: Mapped[str] = mapped_column(Text)
    descripcion: Mapped[str] = mapped_column(Text)
    ciudad: Mapped[str] = mapped_column(Text, server_default=text("'Monterrey'"))
    sugerida_por: Mapped[str] = mapped_column(Text)
    contacto: Mapped[str] = mapped_column(Text)
    cuenta_id: Mapped[uuid.UUID | None] = mapped_column(ForeignKey("cuentas.id", ondelete="SET NULL"))
    estado: Mapped[str] = mapped_column(Text, server_default=text("'pendiente'"))
    detalle_extra: Mapped[str] = mapped_column(Text, server_default=text("''"))
    organizacion: Mapped[str] = mapped_column(Text, server_default=text("''"))
    contacto_causa: Mapped[str] = mapped_column(Text, server_default=text("''"))
    creada_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))
    revisada_en: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class SolicitudEliminacion(Base):
    __tablename__ = "solicitudes_eliminacion"
    id: Mapped[uuid.UUID] = _id()
    folio: Mapped[int] = mapped_column(BigInteger, Identity(always=True))
    cuenta_id: Mapped[uuid.UUID | None] = mapped_column(ForeignKey("cuentas.id", ondelete="SET NULL"))
    correo: Mapped[str] = mapped_column(Text)
    estado: Mapped[str] = mapped_column(Text, server_default=text("'pendiente'"))
    creada_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))
    atendida_en: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class Auditoria(Base):
    __tablename__ = "auditoria"
    id: Mapped[int] = mapped_column(BigInteger, Identity(always=True), primary_key=True)
    ocurrio_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))
    actor_id: Mapped[uuid.UUID | None] = mapped_column(UUID(as_uuid=True))
    accion: Mapped[str] = mapped_column(Text)
    objeto_tipo: Mapped[str] = mapped_column(Text)
    objeto_id: Mapped[str | None] = mapped_column(Text)
    detalle: Mapped[dict] = mapped_column(JSONB, server_default=text("'{}'"))
    ip_hmac: Mapped[str | None] = mapped_column(Text)


class Sesion(Base):
    """Un refresh token (solo su huella). Ver identidad.py y la migración V8."""

    __tablename__ = "sesiones"
    id: Mapped[uuid.UUID] = _id()
    cuenta_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("cuentas.id", ondelete="CASCADE"))
    familia: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True))
    huella: Mapped[str] = mapped_column(Text, unique=True)
    creada_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))
    expira_en: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    usada_en: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    revocada_en: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))


class Recuperacion(Base):
    """Un código para recuperar la contraseña (solo su HMAC). Ver recuperacion.py y la migración V9."""

    __tablename__ = "recuperaciones"
    id: Mapped[uuid.UUID] = _id()
    cuenta_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("cuentas.id", ondelete="CASCADE"))
    huella: Mapped[str] = mapped_column(Text)
    creada_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))
    expira_en: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    usada_en: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    intentos: Mapped[int] = mapped_column(Integer, server_default=text("0"))


class Aviso(Base):
    """Una urgencia de una ciudad o una actividad nueva (RF-22). Ver routers/avisos.py y la migración V9."""

    __tablename__ = "avisos"
    id: Mapped[int] = mapped_column(BigInteger, Identity(always=True), primary_key=True)
    tipo: Mapped[str] = mapped_column(Text)
    ciudad: Mapped[str | None] = mapped_column(Text, ForeignKey("ciudades.nombre"))
    titulo: Mapped[str] = mapped_column(Text)
    cuerpo: Mapped[str] = mapped_column(Text)
    actividad_id: Mapped[uuid.UUID | None] = mapped_column(ForeignKey("actividades.id", ondelete="SET NULL"))
    creado_por: Mapped[uuid.UUID | None] = mapped_column(UUID(as_uuid=True))
    creado_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=text("now()"))
