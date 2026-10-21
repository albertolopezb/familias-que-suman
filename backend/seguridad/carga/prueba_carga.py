"""Prueba de carga contra el servidor de verdad (Azure, por el túnel de Cloudflare).

    python backend/seguridad/carga/prueba_carga.py https://<tu-tunel>.trycloudflare.com

Dos escenarios, los dos desde UNA computadora (una sola IP pública):

  A · "20 familias abren la app al mismo tiempo": 20 hilos que arrancan juntos y piden lo que pide
      la app al abrir (actividades, campañas, proyectos, centros, ciudades): 100 peticiones a la vez.
      Son menos de 120 por minuto, el límite por IP, que es lo que verían 20 familias reales, cada
      una con su propia IP.
  B · "Una sola persona (o un bot) llamando sin parar": después de dejar pasar un minuto, 200
      peticiones lo más rápido posible desde la misma IP. Las primeras pasan; en cuanto se pasa de
      120 por minuto, el servidor contesta 429 y deja de trabajar para ella. Al final se revisa que
      la API siga sana para todos los demás (/v1/salud).

Solo usa rutas públicas de lectura: no crea cuentas ni cambia datos. Escribe el resumen en
docs/evidencia/carga.txt.
"""

import json
import statistics
import sys
import threading
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

BASE = sys.argv[1].rstrip("/") if len(sys.argv) > 1 else "https://hunter-marilyn-larger-frequent.trycloudflare.com"
AL_ABRIR = ["/v1/actividades", "/v1/campanas", "/v1/proyectos", "/v1/centros", "/v1/ciudades"]
SALIDA = Path(__file__).resolve().parents[3] / "docs" / "evidencia" / "carga.txt"


def pedir(ruta: str) -> tuple[int, float, float]:
    """(código, milisegundos, cuándo empezó)."""
    inicio = time.time()
    t0 = time.perf_counter()
    try:
        with urllib.request.urlopen(urllib.request.Request(BASE + ruta, headers={"User-Agent": "prueba-carga-familias"}), timeout=30) as r:
            r.read()
            codigo = r.status
    except urllib.error.HTTPError as e:
        codigo = e.code
    except Exception:
        codigo = 0
    return codigo, (time.perf_counter() - t0) * 1000, inicio


def percentil(valores: list[float], p: float) -> float:
    v = sorted(valores)
    return v[min(len(v) - 1, int(round(p / 100 * (len(v) - 1))))]


def resumen(nombre: str, resultados: list[tuple[int, float, float]], duracion: float) -> str:
    codigos: dict[int, int] = {}
    for c, _, _ in resultados:
        codigos[c] = codigos.get(c, 0) + 1
    ok = [ms for c, ms, _ in resultados if c == 200]
    lineas = [
        f"== {nombre} ==",
        f"peticiones: {len(resultados)} en {duracion:.1f} s",
        "códigos: " + ", ".join(f"{c}×{n}" for c, n in sorted(codigos.items())),
    ]
    if ok:
        lineas.append(
            f"tiempo de las 200 (ms): promedio {statistics.mean(ok):.0f}, mediana {percentil(ok, 50):.0f}, "
            f"p95 {percentil(ok, 95):.0f}, máximo {max(ok):.0f}"
        )
    return "\n".join(lineas)


def escenario_a() -> str:
    barrera = threading.Barrier(20)

    def familia(_: int) -> list[tuple[int, float, float]]:
        barrera.wait()                                    # las 20 "abren la app" en el mismo instante
        with ThreadPoolExecutor(max_workers=5) as p:      # la app pide sus listas en paralelo
            return list(p.map(pedir, AL_ABRIR))

    t0 = time.perf_counter()
    with ThreadPoolExecutor(max_workers=20) as p:
        todas = [r for lista in p.map(familia, range(20)) for r in lista]
    return resumen("A · 20 familias abren la app al mismo tiempo (100 peticiones)", todas, time.perf_counter() - t0)


def escenario_b() -> str:
    t0 = time.perf_counter()
    with ThreadPoolExecutor(max_workers=10) as p:
        todas = list(p.map(pedir, ["/v1/actividades"] * 200))
    texto = resumen("B · una sola IP manda 200 peticiones seguidas", todas, time.perf_counter() - t0)
    orden = sorted(todas, key=lambda r: r[2])
    primera_429 = next((i + 1 for i, r in enumerate(orden) if r[0] == 429), None)
    rechazadas = [ms for c, ms, _ in todas if c == 429]
    if primera_429:
        texto += f"\nla primera 429 llegó en la petición n.º {primera_429}"
    if rechazadas:
        texto += f"\nuna 429 tarda (ms): mediana {percentil(rechazadas, 50):.0f} (se rechaza sin tocar la base)"
    return texto


def salud() -> str:
    codigo, ms, _ = pedir("/v1/salud")
    return f"/v1/salud después de la ráfaga: {codigo} en {ms:.0f} ms"


if __name__ == "__main__":
    print("Escenario A…", flush=True)
    a = escenario_a()
    print(a, "\n\nEsperando 65 s a que se vacíe la ventana del límite…", flush=True)
    time.sleep(65)
    print("Escenario B…", flush=True)
    b = escenario_b()
    s = salud()
    texto = (
        f"Prueba de carga · {time.strftime('%Y-%m-%d %H:%M')} · {BASE.split('//')[-1]}\n"
        "Desde una computadora (una IP) por el túnel de Cloudflare hasta la VM de Azure (B2als v2: 2 vCPU, 4 GB).\n"
        "Límite global por IP: 120 peticiones por minuto (ventana deslizante, contadores en Redis).\n\n"
        f"{a}\n\n{b}\n{s}\n"
    )
    SALIDA.write_text(texto, encoding="utf-8")
    print("\n" + texto)
