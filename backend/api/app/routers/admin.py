"""Administración del catálogo y de la privacidad. Todo aquí pide un PERMISO (claim `permisos` del JWT):

  · administrar:catalogo → crear, editar y borrar asociaciones, actividades, campañas, proyectos y centros.
  · atender:privacidad   → ver y atender las solicitudes de eliminación de datos.

Separado por función a propósito (OWASP API5, "Broken Function Level Authorization", el caso
de Uber 2016): rutas de administrador bajo /v1/admin, cada una con su revisión en el servidor,
y cada cambio queda en la auditoría con quién lo hizo.
"""

import uuid
from datetime import UTC, datetime

from fastapi import APIRouter, Depends, Request, Response, status
from sqlalchemy import func, select, update
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from .. import almacen, convertir, esquemas as e, limites
from ..bitacora import auditar
from ..db import obtener_db
from ..errores import ApiError, no_encontrado
from ..identidad import ADMINISTRAR_CATALOGO, ATENDER_PRIVACIDAD, Sujeto, requiere
from ..limites import limitador, por_cuenta
from ..modelos import (
    Actividad, Asistencia, Asociacion, ArticuloMeta, Campana, Centro, Ciudad, Cuenta, FormaApoyo, Inscripcion,
    OpcionDonacion, Proyecto, PuntoEntrega, SolicitudEliminacion, Sugerencia, Testimonio,
)
from .avisos import aviso_de_actividad_nueva
from .cuenta import solicitud_salida

router = APIRouter(prefix="/v1/admin", tags=["Administración"])
catalogo = requiere(ADMINISTRAR_CATALOGO)


def _ciudad_valida(db: Session, ciudad: str) -> None:
    if db.get(Ciudad, ciudad) is None:
        raise ApiError(422, "Esa ciudad no está disponible.", "ciudad_invalida")


def _borrar(db: Session, objeto, que: str) -> None:
    try:
        db.delete(objeto)
        db.flush()
    except IntegrityError as error:
        db.rollback()
        raise ApiError(409, f"No se puede borrar: {que} tiene historial (inscripciones, actividades o campañas). Edítalo en su lugar.", "tiene_historial") from error


def _vacio_a_none(v: str) -> str | None:
    return v or None


# ─── Asociaciones ───────────────────────────────────────────────────────────

def _aplicar_asociacion(a: Asociacion, d: e.AsociacionEntrada) -> None:
    for campo, valor in d.model_dump().items():
        setattr(a, campo, valor)


@router.post("/asociaciones", response_model=e.AsociacionSalida, status_code=status.HTTP_201_CREATED)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def crear_asociacion(request: Request, d: e.AsociacionEntrada, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    _ciudad_valida(db, d.ciudad)
    a = Asociacion()
    _aplicar_asociacion(a, d)
    db.add(a)
    db.flush()
    auditar(db, "asociacion.crear", "asociacion", a.id, s.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    return a


@router.put("/asociaciones/{asociacion_id}", response_model=e.AsociacionSalida)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def editar_asociacion(request: Request, asociacion_id: uuid.UUID, d: e.AsociacionEntrada, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    a = db.get(Asociacion, asociacion_id) or _no(no_encontrado("Esa asociación"))
    _ciudad_valida(db, d.ciudad)
    _aplicar_asociacion(a, d)
    auditar(db, "asociacion.editar", "asociacion", a.id, s.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    return a


@router.delete("/asociaciones/{asociacion_id}", status_code=status.HTTP_204_NO_CONTENT)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def borrar_asociacion(request: Request, asociacion_id: uuid.UUID, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    a = db.get(Asociacion, asociacion_id) or _no(no_encontrado("Esa asociación"))
    _borrar(db, a, "la asociación")
    auditar(db, "asociacion.borrar", "asociacion", asociacion_id, s.cuenta_id, {"nombre": a.nombre}, request.state.ip_cliente)
    db.commit()
    return Response(status_code=204)


def _no(error: ApiError):
    raise error


# ─── Actividades ────────────────────────────────────────────────────────────

def _aplicar_actividad(db: Session, a: Actividad, d: e.ActividadEntrada) -> None:
    if db.get(Asociacion, d.asociacion_id) is None:
        raise ApiError(422, "Esa asociación no existe.", "asociacion_invalida")
    _ciudad_valida(db, d.ciudad)
    if d.termina_en is not None and d.termina_en <= d.inicia_en:
        raise ApiError(422, "La hora de fin debe ser después de la de inicio.", "horario_invalido")
    datos = d.model_dump(exclude={"lugares_disponibles"})
    datos["imagen"] = _vacio_a_none(datos["imagen"])
    for campo, valor in datos.items():
        setattr(a, campo, valor)
    if d.lugares_disponibles is not None:
        if d.lugares_disponibles > d.cupo_total:
            raise ApiError(422, "No puede haber más lugares disponibles que el cupo.", "cupo_invalido")
        en_app = convertir.ocupados_en_app(db, [a.id]).get(a.id, 0) if a.id else 0
        a.ocupados_fuera = max(d.cupo_total - d.lugares_disponibles - en_app, 0)


@router.post("/actividades", response_model=e.ActividadSalida, status_code=status.HTTP_201_CREATED)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def crear_actividad(request: Request, d: e.ActividadEntrada, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    a = Actividad()
    _aplicar_actividad(db, a, d)
    db.add(a)
    db.flush()
    aviso_de_actividad_nueva(db, a.id, a.titulo, a.ciudad, s.cuenta_id)
    auditar(db, "actividad.crear", "actividad", a.id, s.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    db.refresh(a)
    return convertir.actividades(db, [a])[0]


@router.put("/actividades/{actividad_id}", response_model=e.ActividadSalida)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def editar_actividad(request: Request, actividad_id: uuid.UUID, d: e.ActividadEntrada, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    a = db.get(Actividad, actividad_id) or _no(no_encontrado("Esa actividad"))
    _aplicar_actividad(db, a, d)
    auditar(db, "actividad.editar", "actividad", a.id, s.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    db.refresh(a)
    return convertir.actividades(db, [a])[0]


@router.post("/actividades/{actividad_id}/duplicar", response_model=e.ActividadSalida, status_code=status.HTTP_201_CREATED)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def duplicar_actividad(request: Request, actividad_id: uuid.UUID, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    original = db.get(Actividad, actividad_id) or _no(no_encontrado("Esa actividad"))
    copia = Actividad()
    for columna in Actividad.__table__.columns.keys():
        if columna not in ("id", "creada_en", "ocupados_fuera"):
            setattr(copia, columna, getattr(original, columna))
    copia.titulo = f"{original.titulo} (Copia)"[:160]
    db.add(copia)
    db.flush()
    auditar(db, "actividad.duplicar", "actividad", copia.id, s.cuenta_id, {"original": str(actividad_id)}, request.state.ip_cliente)
    db.commit()
    db.refresh(copia)
    return convertir.actividades(db, [copia])[0]


@router.delete("/actividades/{actividad_id}", status_code=status.HTTP_204_NO_CONTENT)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def borrar_actividad(request: Request, actividad_id: uuid.UUID, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    a = db.get(Actividad, actividad_id) or _no(no_encontrado("Esa actividad"))
    _borrar(db, a, "la actividad")
    auditar(db, "actividad.borrar", "actividad", actividad_id, s.cuenta_id, {"titulo": a.titulo}, request.state.ip_cliente)
    db.commit()
    return Response(status_code=204)


# ─── Campañas ───────────────────────────────────────────────────────────────

def _aplicar_campana(db: Session, c: Campana, d: e.CampanaEntrada) -> None:
    _ciudad_valida(db, d.ciudad)
    if d.asociacion_id is not None and db.get(Asociacion, d.asociacion_id) is None:
        raise ApiError(422, "Esa asociación no existe.", "asociacion_invalida")
    simples = d.model_dump(exclude={"articulos", "opciones_donacion", "puntos_entrega", "completados", "imagen"})
    for campo in ("telefono", "whatsapp", "contacto_nombre", "meta_texto", "instagram"):
        simples[campo] = _vacio_a_none(simples[campo])
    for campo, valor in simples.items():
        setattr(c, campo, valor)
    c.imagen_clave = _vacio_a_none(d.imagen)
    if d.articulos is not None:
        # Por nombre: lo que ya existía conserva sus aportaciones; solo se borra lo que se quitó.
        actuales = {a.nombre.lower(): a for a in c.articulos}
        nuevos = []
        for art in d.articulos:
            existente = actuales.get(art.nombre.lower()) or ArticuloMeta(nombre=art.nombre)
            existente.meta = art.meta
            nuevos.append(existente)
        c.articulos = nuevos
    if d.opciones_donacion is not None:
        c.opciones = [OpcionDonacion(nombre=o.nombre, precio=o.precio, orden=n) for n, o in enumerate(d.opciones_donacion)]
    if d.puntos_entrega is not None:
        c.puntos = [PuntoEntrega(direccion=p.direccion, colonia=p.colonia, orden=n) for n, p in enumerate(d.puntos_entrega)]
    if d.completados is not None:
        db.flush()
        apartados = sum(convertir.apartados_en_app(db, [a.id for a in c.articulos if a.id]).values()) + sum(a.apartados_fuera for a in c.articulos)
        c.completados_fuera = max(d.completados - apartados, 0)


@router.post("/campanas", response_model=e.CampanaSalida, status_code=status.HTTP_201_CREATED)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def crear_campana(request: Request, d: e.CampanaEntrada, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    c = Campana(articulos=[], opciones=[], puntos=[])
    db.add(c)
    _aplicar_campana(db, c, d)
    db.flush()
    auditar(db, "campana.crear", "campana", c.id, s.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    db.refresh(c)
    return convertir.campanas(db, [c])[0]


@router.put("/campanas/{campana_id}", response_model=e.CampanaSalida)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def editar_campana(request: Request, campana_id: uuid.UUID, d: e.CampanaEntrada, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    c = db.get(Campana, campana_id) or _no(no_encontrado("Esa campaña"))
    _aplicar_campana(db, c, d)
    auditar(db, "campana.editar", "campana", c.id, s.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    db.refresh(c)
    return convertir.campanas(db, [c])[0]


@router.delete("/campanas/{campana_id}", status_code=status.HTTP_204_NO_CONTENT)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def borrar_campana(request: Request, campana_id: uuid.UUID, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    c = db.get(Campana, campana_id) or _no(no_encontrado("Esa campaña"))
    _borrar(db, c, "la campaña")
    auditar(db, "campana.borrar", "campana", campana_id, s.cuenta_id, {"titulo": c.titulo}, request.state.ip_cliente)
    db.commit()
    return Response(status_code=204)


# ─── Proyectos ──────────────────────────────────────────────────────────────

def _aplicar_proyecto(p: Proyecto, d: e.ProyectoEntrada) -> None:
    simples = d.model_dump(exclude={"formas_apoyo", "logo"})
    for campo in ("beneficiarios", "vigencia", "telefono", "whatsapp", "instagram"):
        simples[campo] = _vacio_a_none(simples[campo])
    for campo, valor in simples.items():
        setattr(p, campo, valor)
    p.logo_clave = _vacio_a_none(d.logo)
    if d.formas_apoyo is not None:
        p.formas_apoyo = [FormaApoyo(titulo=f.titulo, detalle=f.detalle, icono=f.icono, orden=n) for n, f in enumerate(d.formas_apoyo)]


@router.post("/proyectos", response_model=e.ProyectoSalida, status_code=status.HTTP_201_CREATED)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def crear_proyecto(request: Request, d: e.ProyectoEntrada, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    p = Proyecto(formas_apoyo=[])
    _aplicar_proyecto(p, d)
    db.add(p)
    db.flush()
    auditar(db, "proyecto.crear", "proyecto", p.id, s.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    db.refresh(p)
    return convertir.proyecto(p)


@router.put("/proyectos/{proyecto_id}", response_model=e.ProyectoSalida)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def editar_proyecto(request: Request, proyecto_id: uuid.UUID, d: e.ProyectoEntrada, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    p = db.get(Proyecto, proyecto_id) or _no(no_encontrado("Ese proyecto"))
    _aplicar_proyecto(p, d)
    auditar(db, "proyecto.editar", "proyecto", p.id, s.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    db.refresh(p)
    return convertir.proyecto(p)


@router.delete("/proyectos/{proyecto_id}", status_code=status.HTTP_204_NO_CONTENT)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def borrar_proyecto(request: Request, proyecto_id: uuid.UUID, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    p = db.get(Proyecto, proyecto_id) or _no(no_encontrado("Ese proyecto"))
    _borrar(db, p, "el proyecto")
    auditar(db, "proyecto.borrar", "proyecto", proyecto_id, s.cuenta_id, {"nombre": p.nombre}, request.state.ip_cliente)
    db.commit()
    return Response(status_code=204)


# ─── Centros ────────────────────────────────────────────────────────────────

def _aplicar_centro(db: Session, c: Centro, d: e.CentroEntrada) -> None:
    _ciudad_valida(db, d.ciudad)
    simples = d.model_dump(exclude={"logo"})
    for campo in ("telefono", "whatsapp", "instagram"):
        simples[campo] = _vacio_a_none(simples[campo])
    for campo, valor in simples.items():
        setattr(c, campo, valor)
    c.logo_clave = _vacio_a_none(d.logo)


@router.post("/centros", response_model=e.CentroSalida, status_code=status.HTTP_201_CREATED)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def crear_centro(request: Request, d: e.CentroEntrada, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    c = Centro()
    _aplicar_centro(db, c, d)
    db.add(c)
    db.flush()
    auditar(db, "centro.crear", "centro", c.id, s.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    return convertir.centro(c)


@router.put("/centros/{centro_id}", response_model=e.CentroSalida)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def editar_centro(request: Request, centro_id: uuid.UUID, d: e.CentroEntrada, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    c = db.get(Centro, centro_id) or _no(no_encontrado("Ese centro"))
    _aplicar_centro(db, c, d)
    auditar(db, "centro.editar", "centro", c.id, s.cuenta_id, ip=request.state.ip_cliente)
    db.commit()
    return convertir.centro(c)


@router.delete("/centros/{centro_id}", status_code=status.HTTP_204_NO_CONTENT)
@limitador.limit(limites.ADMIN, key_func=por_cuenta)
def borrar_centro(request: Request, centro_id: uuid.UUID, s: Sujeto = Depends(catalogo), db: Session = Depends(obtener_db)):
    c = db.get(Centro, centro_id) or _no(no_encontrado("Ese centro"))
    _borrar(db, c, "el centro")
    auditar(db, "centro.borrar", "centro", centro_id, s.cuenta_id, {"nombre": c.nombre}, request.state.ip_cliente)
    db.commit()
    return Response(status_code=204)


# ─── Privacidad (HU-14) ─────────────────────────────────────────────────────

@router.get("/solicitudes-eliminacion", response_model=list[e.SolicitudEliminacionSalida])
def solicitudes(s: Sujeto = Depends(requiere(ATENDER_PRIVACIDAD)), db: Session = Depends(obtener_db)):
    filas = db.scalars(select(SolicitudEliminacion).order_by(SolicitudEliminacion.creada_en.desc()).limit(500))
    return [solicitud_salida(x, con_correo=True) for x in filas]


@router.post("/solicitudes-eliminacion/{solicitud_id}/atender", response_model=e.SolicitudEliminacionSalida)
def atender(request: Request, solicitud_id: uuid.UUID, s: Sujeto = Depends(requiere(ATENDER_PRIVACIDAD)), db: Session = Depends(obtener_db)):
    """Borra la cuenta y TODO lo suyo (las tablas lo hacen en cascada) y sus fotos del almacén.
    La solicitud se queda, sin la cuenta, como constancia. Con la cuenta se va su contraseña
    (el hash): ese correo ya no puede entrar, y sus tokens vigentes dejan de servir porque la
    cuenta ya no existe."""
    solicitud = db.get(SolicitudEliminacion, solicitud_id) or _no(no_encontrado("Esa solicitud"))
    if solicitud.estado != "pendiente":
        return solicitud_salida(solicitud, con_correo=True)
    fotos: list[str] = []
    if solicitud.cuenta_id is not None:
        cuenta = db.get(Cuenta, solicitud.cuenta_id)
        if cuenta is not None:
            fotos = [cuenta.foto_clave] if cuenta.foto_clave else []
            fotos += [f for f in db.scalars(
                select(Testimonio.foto_clave).join(Asistencia, Testimonio.asistencia_id == Asistencia.id)
                .where(Asistencia.cuenta_id == cuenta.id, Testimonio.foto_clave.is_not(None))
            )]
            # Las sugerencias que mandó se quedan (son sobre causas, no sobre ella), pero sin su
            # nombre ni su contacto: así no queda ningún dato suyo (aviso de privacidad, LFPDPPP art. 24).
            db.execute(
                update(Sugerencia).where(Sugerencia.cuenta_id == cuenta.id)
                .values(sugerida_por="Familia que eliminó su cuenta", contacto="")
            )
            db.delete(cuenta)
    solicitud.estado = "atendida"
    solicitud.atendida_en = datetime.now(UTC)
    auditar(db, "privacidad.eliminar_cuenta", "solicitud", solicitud.id, s.cuenta_id, {"folio": solicitud.folio}, request.state.ip_cliente)
    db.commit()
    for foto in fotos:
        almacen.borrar(foto)
    db.refresh(solicitud)
    return solicitud_salida(solicitud, con_correo=True)
