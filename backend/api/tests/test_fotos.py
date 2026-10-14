"""Subida de fotos (CWE-434): solo fotos de verdad, de tamaño razonable, reescritas sin metadatos."""

import io
import struct
import zlib

from PIL import Image


def _jpeg_con_gps() -> bytes:
    imagen = Image.new("RGB", (64, 48), (10, 120, 200))
    exif = Image.Exif()
    exif[0x010F] = "Marca del teléfono"
    # GPSInfo: dónde se tomó la foto (muchas veces, la casa de la familia).
    exif.get_ifd(0x8825).update({1: "N", 2: (25.0, 40.0, 10.0), 3: "W", 4: (100.0, 18.0, 30.0)})
    salida = io.BytesIO()
    imagen.save(salida, "JPEG", exif=exif)
    return salida.getvalue()


def _png_bomba() -> bytes:
    """Un PNG de pocos bytes que DICE medir 50,000 × 50,000 (2,500 megapíxeles)."""
    def trozo(tipo: bytes, datos: bytes) -> bytes:
        return struct.pack(">I", len(datos)) + tipo + datos + struct.pack(">I", zlib.crc32(tipo + datos))
    ihdr = struct.pack(">IIBBBBB", 50_000, 50_000, 8, 2, 0, 0, 0)
    return b"\x89PNG\r\n\x1a\n" + trozo(b"IHDR", ihdr) + trozo(b"IDAT", zlib.compress(b"\x00" * 100)) + trozo(b"IEND", b"")


def _subir(cliente, persona, datos: bytes, nombre="foto.jpg", tipo="image/jpeg"):
    return cliente.put("/v1/yo/foto", headers=persona.headers, files={"foto": (nombre, datos, tipo)})


def test_foto_valida_se_guarda_sin_exif(cliente, familia):
    r = _subir(cliente, familia, _jpeg_con_gps())
    assert r.status_code == 200, r.text
    assert r.json()["tiene_foto"] is True
    guardada = cliente.get("/v1/yo/foto", headers=familia.headers)
    assert guardada.status_code == 200
    assert guardada.headers["content-type"] == "image/jpeg"
    with Image.open(io.BytesIO(guardada.content)) as im:
        assert im.format == "JPEG"
        assert len(im.getexif()) == 0                      # ni GPS, ni marca del teléfono


def test_html_disfrazado_de_jpg_se_rechaza(cliente, familia):
    r = _subir(cliente, familia, b"<html><script>alert(1)</script></html>", nombre="gatito.jpg", tipo="image/jpeg")
    assert r.status_code == 422 and r.json()["codigo"] == "foto_invalida"


def test_poliglota_jpeg_con_script_se_limpia(cliente, familia):
    # Una foto válida con HTML pegado al final: al reescribirla, lo pegado desaparece.
    r = _subir(cliente, familia, _jpeg_con_gps() + b"<script>alert(1)</script>")
    assert r.status_code == 200
    assert b"<script>" not in cliente.get("/v1/yo/foto", headers=familia.headers).content


def test_bomba_de_descompresion_se_rechaza(cliente, familia):
    r = _subir(cliente, familia, _png_bomba(), nombre="bomba.png", tipo="image/png")
    assert r.status_code == 422


def test_foto_demasiado_grande(cliente, familia):
    r = _subir(cliente, familia, b"\xff\xd8" + b"0" * 6_100_000)
    assert r.status_code == 413


def test_la_foto_de_perfil_no_la_ve_otra_familia(cliente, familia, otra_familia):
    _subir(cliente, familia, _jpeg_con_gps())
    from .conftest import conectar_como_dueno
    with conectar_como_dueno() as c:
        clave = c.execute("SELECT foto_clave FROM cuentas WHERE auth0_sub = %s", (familia.sub,)).fetchone()[0]
    assert cliente.get(f"/v1/fotos/{clave}").status_code == 401                            # sin sesión
    assert cliente.get(f"/v1/fotos/{clave}", headers=otra_familia.headers).status_code == 404
    assert cliente.get(f"/v1/fotos/{clave}", headers=familia.headers).status_code == 200


def test_nombres_de_foto_raros_no_llegan_al_almacen(cliente):
    for nombre in ["..%2f..%2fetc%2fpasswd", "x.jpg", "a" * 32 + ".php"]:
        assert cliente.get(f"/v1/fotos/testimonios/{nombre}").status_code in (404, 422)
