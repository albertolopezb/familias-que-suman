"""El almacén de fotos (S3). Todo lo demás le habla a estas funciones y no sabe que del otro
lado hay RustFS: con cambiar S3_ENDPOINT y las llaves, funciona igual con Amazon S3 o R2.

El almacén NUNCA está en internet: solo la API lo ve (red `datos` de Docker). La app pide
las fotos a la API, que revisa permisos y luego las lee de aquí. Las claves de los objetos las
genera la API (uuid), nunca la persona: así no hay forma de pedir "../../otra-cosa" (CWE-22).
"""

import re
from typing import Any

import boto3
from botocore.config import Config
from botocore.exceptions import BotoCoreError, ClientError

from .config import ajustes
from .errores import ApiError

_s3 = boto3.client(
    "s3",
    endpoint_url=ajustes.s3_endpoint,
    aws_access_key_id=ajustes.s3_access_key,
    aws_secret_access_key=ajustes.s3_secret_key,
    region_name="us-east-1",
    config=Config(s3={"addressing_style": "path"}, connect_timeout=3, read_timeout=10, retries={"max_attempts": 2}),
)

CLAVE_VALIDA = re.compile(r"(perfil|testimonios)/[0-9a-f]{32}\.jpg")


def _no_disponible(e: Exception) -> ApiError:
    return ApiError(503, "Las fotos no están disponibles en este momento.", "almacen_no_disponible")


def preparar() -> None:
    """Crea el bucket si no existe. Es privado: no se le pone ninguna política pública."""
    try:
        _s3.head_bucket(Bucket=ajustes.s3_bucket)
    except ClientError:
        _s3.create_bucket(Bucket=ajustes.s3_bucket)


def guardar(clave: str, datos: bytes) -> None:
    try:
        _s3.put_object(Bucket=ajustes.s3_bucket, Key=clave, Body=datos, ContentType="image/jpeg")
    except (ClientError, BotoCoreError) as e:
        raise _no_disponible(e) from e


def abrir(clave: str) -> dict[str, Any] | None:
    if not CLAVE_VALIDA.fullmatch(clave):
        return None
    try:
        return _s3.get_object(Bucket=ajustes.s3_bucket, Key=clave)
    except ClientError as e:
        if e.response["Error"]["Code"] in ("NoSuchKey", "404"):
            return None
        raise _no_disponible(e) from e
    except BotoCoreError as e:
        raise _no_disponible(e) from e


def borrar(clave: str | None) -> None:
    if not clave or not CLAVE_VALIDA.fullmatch(clave):
        return
    try:
        _s3.delete_object(Bucket=ajustes.s3_bucket, Key=clave)
    except (ClientError, BotoCoreError):
        # Una foto que no se pudo borrar no detiene la operación: queda huérfana y se puede
        # limpiar después. Lo importante (el registro) ya se borró.
        pass


def disponible() -> bool:
    try:
        _s3.head_bucket(Bucket=ajustes.s3_bucket)
        return True
    except (ClientError, BotoCoreError):
        return False
