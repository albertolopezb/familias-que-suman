# Backend de Familias que Suman

API (FastAPI) + PostgreSQL + almacén de fotos S3 (RustFS) + Redis + identidad con **Auth0**,
monitoreo con **Prometheus/Grafana**, salida a internet por **Cloudflare Tunnel** y escaneo con
**OWASP ZAP**. Todo en Docker.

> La guía completa —qué es cada pieza, cómo protege, de qué protege, dónde está en el código y
> por qué se decidió así— está en [`docs/guia-backend.md`](../docs/guia-backend.md).

## Levantarlo

Con Docker Desktop abierto:

```bash
cd backend
docker compose up -d --build
```

Eso crea la base, aplica las migraciones (y el usuario de mínimo privilegio de la API), carga los
datos de prueba, prepara el almacén y Redis, y levanta la API, Prometheus y Grafana.

| Qué | Dónde | Notas |
|---|---|---|
| API | http://localhost:8000/v1/salud | El emulador llega por `http://10.0.2.2:8000` |
| Documentación (OpenAPI) | http://localhost:8000/docs | Solo en desarrollo y solo desde tu máquina |
| Grafana | http://localhost:3000 | `admin` / la de `GRAFANA_CONTRASENA` |
| Prometheus | http://localhost:9090 | Alertas en `/alerts` |
| Consola del almacén | http://localhost:9001 | Usuario y contraseña: `STORAGE_ROOT_*` |

Sin `.env` funciona con valores de desarrollo. Para cambiar puertos o contraseñas: copia
`.env.example` a `.env`. **Auth0** se configura ahí (ver la guía, §4).

**Si vienes de la versión anterior** (MinIO, cuentas con contraseña en la base): esta versión
cambia el esquema (V6) y los datos de prueba. Para empezar limpio: `docker compose down -v` y
luego `docker compose up -d --build` (borra los datos de tu máquina).

## Lo demás

```bash
docker compose --profile tunel up -d                    # el túnel HTTPS (internet → API)
docker compose logs tunel | grep trycloudflare          # la dirección pública
docker compose --profile pruebas run --rm --build pruebas    # las pruebas automáticas (102)
docker compose --profile zap run --rm zap               # el escaneo de OWASP ZAP → seguridad/zap/
docker compose exec db psql -U familias -d familias     # entrar a la base
docker compose down                                     # apagar (conserva los datos)
```

## Qué es cada carpeta

| Carpeta o archivo | Qué es |
|---|---|
| `docker-compose.yml` | Todos los contenedores, sus redes y su endurecimiento |
| `api/` | La API: `app/` (código), `tests/` (pruebas), `Dockerfile`, `pyproject.toml` + `uv.lock` |
| `db/migrations/` | Migraciones de Flyway (`V1`…`V6`) y `afterMigrate.sql` (permisos de la API) |
| `db/seed/dev.sql` | Datos de prueba (solo desarrollo) |
| `monitoreo/` | Configuración de Prometheus (y sus alertas) y de Grafana (fuente y tablero) |
| `seguridad/zap/` | Plan, reglas y reportes de OWASP ZAP |

**Migraciones, en una frase:** cada archivo cambia la base una sola vez, en orden, y Flyway apunta
cuáles ya corrió. Para cambiar algo **no se edita un archivo viejo: se crea el siguiente** (`V7__…`).
