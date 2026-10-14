"""Las fotos que suben las familias (perfil y testimonios). Subir archivos es de lo más
peligroso de una API (CWE-434 · Unrestricted Upload), así que cada foto pasa por esto:

  1. Tamaño: más de 6 MB ni se lee (también lo corta seguridad.LimiteCuerpo).
  2. Tipo REAL: se abre con Pillow, que mira los bytes del archivo (la "firma"), no la
     extensión ni el Content-Type que dice el teléfono, que se pueden falsificar.
     Solo se aceptan JPEG, PNG y WebP.
  3. Bomba de descompresión: un PNG de 50 KB puede decir que mide 100,000 × 100,000 píxeles
     y reventar la memoria al abrirlo. Pillow se niega a pasar de 40 megapíxeles.
  4. Se VUELVE A CODIFICAR como JPEG nuevo, reducido a 1600 px. Lo que se guarda ya no es el
     archivo que mandaron: si traía algo escondido (un script, un archivo "políglota"), no
     sobrevive. Y se pierden los metadatos EXIF, incluida la UBICACIÓN GPS de la casa de la
     familia que muchas cámaras guardan en cada foto. Privacidad por diseño.
"""

import io
import uuid
import warnings

from PIL import Image, ImageOps, UnidentifiedImageError

from .config import ajustes
from .errores import ApiError
from . import metricas

Image.MAX_IMAGE_PIXELS = 40_000_000
# Pillow solo AVISA entre 40 y 80 MP; así el aviso también se vuelve un rechazo.
warnings.simplefilter("error", Image.DecompressionBombWarning)
FORMATOS = {"JPEG", "PNG", "WEBP"}


def _rechazo(mensaje: str) -> ApiError:
    metricas.evento_seguridad("archivo_rechazado")
    return ApiError(422, mensaje, "foto_invalida")


def procesar(datos: bytes) -> bytes:
    if not datos:
        raise _rechazo("No llegó ninguna foto.")
    if len(datos) > ajustes.max_foto_bytes:
        raise _rechazo("La foto pesa demasiado (máximo 6 MB).")
    try:
        with Image.open(io.BytesIO(datos)) as original:
            if original.format not in FORMATOS:
                raise _rechazo("Solo se aceptan fotos JPEG, PNG o WebP.")
            original.load()
            imagen = ImageOps.exif_transpose(original)   # respeta la orientación antes de tirar el EXIF
            imagen.thumbnail((ajustes.max_foto_lado, ajustes.max_foto_lado))
            if imagen.mode not in ("RGB", "L"):
                fondo = Image.new("RGB", imagen.size, (255, 255, 255))
                imagen = imagen.convert("RGBA")
                fondo.paste(imagen, mask=imagen.split()[-1])
                imagen = fondo
            salida = io.BytesIO()
            imagen.convert("RGB").save(salida, "JPEG", quality=85, optimize=True)  # sin exif=…: sale limpio
            return salida.getvalue()
    except ApiError:
        raise
    except (UnidentifiedImageError, Image.DecompressionBombError, Image.DecompressionBombWarning, OSError, ValueError) as e:
        raise _rechazo("El archivo no es una foto válida.") from e


def clave_nueva(carpeta: str) -> str:
    """El nombre lo pone el servidor: 32 caracteres al azar. Nunca el que mandó el teléfono."""
    return f"{carpeta}/{uuid.uuid4().hex}.jpg"
