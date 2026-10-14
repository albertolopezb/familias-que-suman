"""Los enlaces para invitar a otra familia, y cómo abren la APP en lugar del sitio web.

    https://<api>/a/<id-de-la-actividad>

1. Con dominio propio (túnel con nombre): Android revisa, al instalar la app, el archivo
   https://<dominio>/.well-known/assetlinks.json, que dice "este dominio confía en la app
   mx.tec.familiasquesuman firmada con ESTA huella". Si coincide, tocar el enlace abre la app
   directo, sin preguntar (Android App Links verificados).
2. Con Quick Tunnel (dirección *.trycloudflare.com que cambia), Android no puede verificar el
   dominio, así que el enlace abre esta página mínima: un botón "Abrir en la app" que usa
   `intent://…` y, si la app no está instalada, manda al sitio de Familias que Suman.

La página es HTML hecho en el servidor con datos de la base (el título de la actividad), así
que es el lugar donde podría colarse un XSS (OWASP A05 · CWE-79). Todo lo que viene de la base
pasa por html.escape() antes de entrar al HTML (como en la clase), y además la CSP prohíbe
cualquier script: aunque algo se colara, el navegador no lo ejecutaría.
"""

import html
import json
import re
import time
import urllib.parse
import urllib.request
import uuid

from fastapi import APIRouter, Depends, Request
from fastapi.responses import HTMLResponse, JSONResponse, Response
from sqlalchemy.orm import Session

from .. import convertir, esquemas as e, limites
from ..config import ajustes
from ..db import obtener_db
from ..errores import no_encontrado
from ..limites import limitador
from ..modelos import Actividad

router = APIRouter(tags=["Enlaces"])

_HOST_TUNEL = re.compile(r"[a-z0-9-]{1,63}\.trycloudflare\.com")
_cache_tunel: dict[str, float | str] = {"valor": "", "hasta": 0.0}


def _host_del_tunel() -> str:
    """La dirección pública del Quick Tunnel: cloudflared la publica en su puerto de métricas.
    La URL es FIJA (de la configuración, nunca de una petición): no hay SSRF posible."""
    if time.monotonic() < float(_cache_tunel["hasta"]):
        return str(_cache_tunel["valor"])
    host = ""
    try:
        with urllib.request.urlopen(f"{ajustes.tunel_metricas_url}/quicktunnel", timeout=2) as r:  # noqa: S310
            candidato = str(json.loads(r.read(4096)).get("hostname", "")).lower()
            if _HOST_TUNEL.fullmatch(candidato):
                host = candidato
    except (OSError, ValueError):
        pass
    _cache_tunel.update(valor=host, hasta=time.monotonic() + (60 if host else 10))
    return host


def base_publica(request: Request) -> str:
    if ajustes.enlaces_base:
        return ajustes.enlaces_base.rstrip("/")
    host = _host_del_tunel()
    if host:
        return f"https://{host}"
    return str(request.base_url).rstrip("/")


@router.get("/v1/actividades/{actividad_id}/enlace", response_model=e.EnlaceSalida, tags=["Catálogo (público)"])
def enlace(request: Request, actividad_id: uuid.UUID, db: Session = Depends(obtener_db)):
    if db.get(Actividad, actividad_id) is None:
        raise no_encontrado("Esa actividad")
    return e.EnlaceSalida(url=f"{base_publica(request)}/a/{actividad_id}")


_ESTILO = """
body{margin:0;font-family:system-ui,-apple-system,Segoe UI,Roboto,sans-serif;background:#0E2148;color:#fff;
display:flex;min-height:100vh;align-items:center;justify-content:center;padding:16px}
main{max-width:420px;width:100%;text-align:center}
.marca{font-weight:800;letter-spacing:.06em;color:#F5B83D;font-size:14px;margin-bottom:24px}
h1{font-size:26px;line-height:1.2;margin:0 0 12px}
p{opacity:.85;margin:0 0 8px;font-size:16px}
a.boton{display:block;margin:28px 0 12px;padding:16px;border-radius:999px;background:#fff;color:#1E293B;
font-weight:700;text-decoration:none;font-size:17px}
a.sec{color:#fff;opacity:.8;font-size:14px}
"""
_HASH_ESTILO = None


def _hash_estilo() -> str:
    # La CSP permite exactamente ESTE bloque de estilo (por su SHA-256) y nada más.
    global _HASH_ESTILO
    if _HASH_ESTILO is None:
        import base64
        import hashlib
        _HASH_ESTILO = base64.b64encode(hashlib.sha256(_ESTILO.encode()).digest()).decode()
    return _HASH_ESTILO


@router.get("/a/{actividad_id}", response_class=HTMLResponse, include_in_schema=False)
@limitador.limit(limites.ENLACE_PUBLICO)
def pagina_invitacion(request: Request, actividad_id: uuid.UUID, db: Session = Depends(obtener_db)):
    a = db.get(Actividad, actividad_id)
    if a is None:
        return HTMLResponse(_pagina("Esta actividad ya no está disponible", "", "", None), status_code=404, headers=_csp())
    zona = convertir.zona_de(db, a.ciudad)
    inicio = a.inicia_en.astimezone(zona)
    cuando = f"{inicio:%d/%m/%Y} · {inicio:%H:%M} h"
    lugar = a.direccion or a.municipio
    return HTMLResponse(_pagina(a.titulo, cuando, lugar, actividad_id), headers=_csp())


def _csp() -> dict[str, str]:
    return {"Content-Security-Policy": f"default-src 'none'; style-src 'sha256-{_hash_estilo()}'; "
                                       "base-uri 'none'; form-action 'none'; frame-ancestors 'none'",
            "Cache-Control": "public, max-age=300"}


def _pagina(titulo: str, cuando: str, lugar: str, actividad_id: uuid.UUID | None) -> str:
    t, c, l = html.escape(titulo), html.escape(cuando), html.escape(lugar)
    respaldo = html.escape(ajustes.sitio_respaldo)
    boton = ""
    if actividad_id:
        intento = (
            f"intent://actividad/{actividad_id}#Intent;scheme={ajustes.app_esquema};package={ajustes.app_paquete};"
            f"S.browser_fallback_url={urllib.parse.quote(ajustes.sitio_respaldo, safe='')};end"
        )
        boton = f'<a class="boton" href="{html.escape(intento)}">Abrir en la app</a>'
    return f"""<!doctype html>
<html lang="es"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="robots" content="noindex">
<title>{t} · Familias que Suman</title>
<meta property="og:title" content="{t}">
<meta property="og:description" content="{c} · {l}">
<meta property="og:site_name" content="Familias que Suman">
<style>{_ESTILO}</style></head>
<body><main>
<div class="marca">FAMILIAS QUE SUMAN</div>
<h1>{t}</h1><p>{c}</p><p>{l}</p>
{boton}
<a class="sec" href="{respaldo}">Ver en familiasquesuman.com</a>
</main></body></html>"""


@router.get("/.well-known/assetlinks.json", include_in_schema=False)
def assetlinks():
    """La prueba de que este dominio confía en la app (Android App Links)."""
    cuerpo = [{
        "relation": ["delegate_permission/common.handle_all_urls"],
        "target": {"namespace": "android_app", "package_name": ajustes.app_paquete, "sha256_cert_fingerprints": ajustes.lista_huellas},
    }]
    return JSONResponse(cuerpo, headers={"Cache-Control": "public, max-age=3600"})


@router.get("/robots.txt", include_in_schema=False)
def robots():
    return Response("User-agent: *\nDisallow: /\n", media_type="text/plain")
