# La imagen de la API. Dos etapas:
#   · produccion → solo lo que necesita correr (sin pytest), con un usuario sin privilegios.
#   · pruebas    → lo mismo + las dependencias de desarrollo y las pruebas.
# docker-compose.yml elige la etapa con `target:`.

FROM python:3.13-slim-bookworm AS base
COPY --from=ghcr.io/astral-sh/uv:0.12.9 /uv /bin/uv

ENV UV_LINK_MODE=copy \
    UV_COMPILE_BYTECODE=1 \
    UV_PROJECT_ENVIRONMENT=/opt/venv \
    PATH="/opt/venv/bin:$PATH" \
    PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1
WORKDIR /code

# Usuario sin privilegios: si alguien lograra ejecutar código dentro del contenedor, no sería root.
RUN groupadd --system --gid 10001 api && useradd --system --uid 10001 --gid api --no-create-home api

COPY pyproject.toml uv.lock ./


FROM base AS produccion
# --frozen: exactamente lo de uv.lock, con sus hashes. --no-dev: sin herramientas de prueba.
RUN uv sync --frozen --no-dev --no-install-project
COPY app ./app
USER api
EXPOSE 8000 9100
HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
    CMD ["python", "-c", "import urllib.request,sys; sys.exit(0 if urllib.request.urlopen('http://127.0.0.1:8000/v1/salud', timeout=2).status == 200 else 1)"]
# --no-server-header: no anunciar "uvicorn".  --limit-concurrency: si llegan más de 200 a la vez,
# el resto recibe 503 rápido en lugar de tumbar el proceso.  --timeout-keep-alive: no dejar
# conexiones ociosas abiertas (al estilo de un SYN flood, pero en la capa de aplicación).
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--no-server-header", \
     "--limit-concurrency", "200", "--timeout-keep-alive", "5", "--h11-max-incomplete-event-size", "16384"]


FROM base AS pruebas
RUN uv sync --frozen --no-install-project
COPY app ./app
COPY tests ./tests
USER api
CMD ["pytest", "-q"]
