"""La bitácora: logs en JSON para la máquina y la tabla `auditoria` para lo sensible.

Privacidad: la IP no se guarda en claro. Se guarda HMAC-SHA256(llave secreta, IP). Con eso se
puede saber que diez intentos vinieron "de la misma IP" sin saber cuál era, y sin la llave
nadie puede revertirlo probando todas las IPs (lo que sí pasaría con un SHA-256 simple: hay
solo 4 mil millones de IPv4). Es el HMAC de la clase de criptografía.
"""

import hashlib
import hmac
import json
import logging
import sys
import uuid
from datetime import UTC, datetime
from typing import Any

from sqlalchemy.orm import Session

from .config import ajustes

_CAMPOS_EXTRA = ("solicitud", "metodo", "ruta", "estado", "ms", "cuenta", "ip_hmac", "via_tunel", "evento")


class FormatoJson(logging.Formatter):
    def format(self, registro: logging.LogRecord) -> str:
        linea: dict[str, Any] = {
            "ts": datetime.fromtimestamp(registro.created, UTC).isoformat(timespec="milliseconds"),
            "nivel": registro.levelname,
            "origen": registro.name,
            "mensaje": registro.getMessage(),
        }
        for campo in _CAMPOS_EXTRA:
            valor = getattr(registro, campo, None)
            if valor is not None:
                linea[campo] = str(valor) if isinstance(valor, uuid.UUID) else valor
        if registro.exc_info:
            # La traza va SOLO a la bitácora del servidor, nunca en la respuesta.
            linea["traza"] = self.formatException(registro.exc_info)
        return json.dumps(linea, ensure_ascii=False)


def configurar() -> None:
    manejador = logging.StreamHandler(sys.stdout)
    manejador.setFormatter(FormatoJson())
    raiz = logging.getLogger()
    raiz.handlers = [manejador]
    raiz.setLevel(logging.INFO)
    # uvicorn ya no escribe su propia línea por petición: la nuestra trae más contexto.
    logging.getLogger("uvicorn.access").disabled = True
    for ruidoso in ("botocore", "boto3", "urllib3", "s3transfer"):
        logging.getLogger(ruidoso).setLevel(logging.WARNING)


def ip_hmac(ip: str | None) -> str | None:
    if not ip:
        return None
    return hmac.new(ajustes.hmac_secreto.encode(), ip.encode(), hashlib.sha256).hexdigest()[:32]


def auditar(
    db: Session,
    accion: str,
    objeto_tipo: str,
    objeto_id: Any = None,
    actor_id: uuid.UUID | None = None,
    detalle: dict[str, Any] | None = None,
    ip: str | None = None,
) -> None:
    """Agrega un renglón a la auditoría en la MISMA transacción que la operación: si la
    operación se deshace, su renglón también; si se guarda, queda constancia."""
    from .modelos import Auditoria

    db.add(
        Auditoria(
            actor_id=actor_id,
            accion=accion,
            objeto_tipo=objeto_tipo,
            objeto_id=str(objeto_id) if objeto_id is not None else None,
            detalle=detalle or {},
            ip_hmac=ip_hmac(ip),
        )
    )
    logging.getLogger("familias.auditoria").info(
        accion, extra={"evento": f"{objeto_tipo}:{objeto_id}", "cuenta": actor_id}
    )
