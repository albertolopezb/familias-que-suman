"""Cuántos hay de cada cosa: lo que ve la administración en «Resumen» y lo que pinta Grafana.

Son solo conteos (COUNT/SUM en PostgreSQL); ningún nombre, correo ni dato de una persona sale de
aquí. Prometheus pide /metrics cada 15 s, así que el resultado se guarda 60 s para no consultar
la base de más.
"""

import time
from datetime import UTC, datetime
from threading import Lock

from prometheus_client.core import GaugeMetricFamily
from prometheus_client.registry import REGISTRY, Collector
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from . import esquemas as e
from .db import SesionLocal
from .modelos import (
    Acompanante, Actividad, Aportacion, Asociacion, Campana, Centro, Cuenta, Inscripcion, Proyecto,
    SolicitudEliminacion, Sugerencia, Testimonio,
)

TIPOS_SUGERENCIA = ("actividad", "campana", "proyecto", "centro")


def contar(db: Session) -> e.ResumenSalida:
    ahora = datetime.now(UTC)
    n = lambda consulta: db.scalar(consulta) or 0  # noqa: E731
    contar_de = lambda modelo, *donde: n(select(func.count()).select_from(modelo).where(*donde))  # noqa: E731

    es_admin = func.cardinality(Cuenta.permisos) > 0
    vigente = (Inscripcion.cancelada_en.is_(None), Actividad.inicia_en >= ahora)
    inscripciones = db.execute(
        select(func.count(), func.coalesce(func.sum(Inscripcion.personas), 0))
        .select_from(Inscripcion).join(Actividad, Inscripcion.actividad_id == Actividad.id).where(*vigente)
    ).one()
    por_tipo = dict(db.execute(select(Sugerencia.tipo, func.count()).group_by(Sugerencia.tipo)).all())

    return e.ResumenSalida(
        familias=contar_de(Cuenta, ~es_admin),
        administradores=contar_de(Cuenta, es_admin),
        acompanantes=contar_de(Acompanante),
        actividades_proximas=contar_de(Actividad, Actividad.inicia_en >= ahora),
        actividades_pasadas=contar_de(Actividad, Actividad.inicia_en < ahora),
        proyectos_activos=contar_de(Proyecto, Proyecto.activo.is_(True)),
        campanas=contar_de(Campana),
        centros=contar_de(Centro),
        asociaciones=contar_de(Asociacion),
        inscripciones_vigentes=inscripciones[0],
        personas_inscritas=int(inscripciones[1]),
        aportaciones=n(select(func.coalesce(func.sum(Aportacion.cantidad), 0))),
        testimonios_publicados=contar_de(Testimonio, Testimonio.estado == "aprobado"),
        testimonios_por_revisar=contar_de(Testimonio, Testimonio.estado == "pendiente"),
        sugerencias={t: por_tipo.get(t, 0) for t in TIPOS_SUGERENCIA},
        sugerencias_por_revisar=contar_de(Sugerencia, Sugerencia.estado == "pendiente"),
        eliminaciones_pendientes=contar_de(SolicitudEliminacion, SolicitudEliminacion.estado == "pendiente"),
    )


class _Registros(Collector):
    """La métrica `familias_registros{tipo=…}` que Grafana muestra en «Cuántos hay»."""

    def __init__(self) -> None:
        self._candado = Lock()
        self._hasta = 0.0
        self._ultimo: e.ResumenSalida | None = None

    def _resumen(self) -> e.ResumenSalida | None:
        with self._candado:
            if time.monotonic() >= self._hasta:
                try:
                    with SesionLocal() as db:
                        self._ultimo = contar(db)
                except Exception:  # sin base, Prometheus sigue recibiendo lo demás
                    self._ultimo = None
                self._hasta = time.monotonic() + 60
            return self._ultimo

    def collect(self):
        g = GaugeMetricFamily("familias_registros", "Cuántos registros hay de cada tipo (solo conteos)", labels=["tipo"])
        r = self._resumen()
        if r is not None:
            for campo, valor in r.model_dump().items():
                if isinstance(valor, dict):
                    for tipo, cuantos in valor.items():
                        g.add_metric([f"{campo}_{tipo}"], cuantos)
                else:
                    g.add_metric([campo], valor)
        yield g

    def describe(self):  # sin esto, registrarlo consultaría la base al arrancar
        return [GaugeMetricFamily("familias_registros", "", labels=["tipo"])]


def registrar_en_prometheus() -> None:
    REGISTRY.register(_Registros())
