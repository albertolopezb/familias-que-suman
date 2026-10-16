# Backend de Familias que Suman

API (FastAPI) + PostgreSQL + almacén de fotos S3 (RustFS) + Redis, con **identidad propia en JWT**
(HS256, contraseñas con Argon2id) y **autorización ABAC con Oso** (reglas en Polar), como en la guía
de clase s23. Monitoreo con **Prometheus/Grafana**, salida a internet por **Cloudflare Tunnel** y
escaneo con **OWASP ZAP**. Todo en Docker.

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
`.env.example` a `.env` (ahí va también `JWT_SECRETO`, el secreto que firma los tokens).

**Cuentas de prueba** (solo desarrollo, las carga el seed):

| Correo | Contraseña | Qué es |
|---|---|---|
| `ana.rodriguez@correo.com` | `familia123` | Familia con historial, testimonios e inscripción |
| `admin@correo.com` | `admin123` | Administración (los cuatro permisos) |

```bash
curl -s -X POST http://localhost:8000/v1/sesion/iniciar -H "Content-Type: application/json"   -d '{"correo":"ana.rodriguez@correo.com","contrasena":"familia123"}'      # → {"access_token": …}
```

**Si vienes de la versión con Auth0:** `docker compose up -d --build` aplica la V7 y le pone
contraseña a las cuentas de prueba; las variables `AUTH0_*` de tu `.env` ya no se usan.

## Lo demás

```bash
docker compose --profile tunel up -d                    # el túnel HTTPS (internet → API)
docker compose logs tunel | grep trycloudflare          # la dirección pública
docker compose --profile pruebas run --rm --build pruebas    # las pruebas automáticas (147)
bash smoke.sh                                           # prueba de contrato contra la API (27 casos)
bash smoke.sh https://<algo>.trycloudflare.com          # la misma, por el túnel
bash seguridad/zap/escanear.sh                          # OWASP ZAP sin y con sesión → seguridad/zap/
docker compose exec db psql -U familias -d familias     # entrar a la base
docker compose exec api python -m app.herramientas permisos <correo> todos    # hacer admin a una cuenta
docker compose exec -it api python -m app.herramientas contrasena <correo>    # ponerle contraseña nueva
docker compose down                                     # apagar (conserva los datos)
```

## Qué es cada carpeta

| Carpeta o archivo | Qué es |
|---|---|
| `docker-compose.yml` | Todos los contenedores, sus redes y su endurecimiento |
| `api/` | La API: `app/` (código), `tests/` (pruebas), `Dockerfile`, `pyproject.toml` + `uv.lock` |
| `db/migrations/` | Migraciones de Flyway (`V1`…`V7`) y `afterMigrate.sql` (permisos de la API) |
| `db/seed/dev.sql` | Datos de prueba (solo desarrollo) |
| `monitoreo/` | Configuración de Prometheus (y sus alertas) y de Grafana (fuente y tablero) |
| `seguridad/zap/` | Plan, reglas y reportes de OWASP ZAP |

**Migraciones, en una frase:** cada archivo cambia la base una sola vez, en orden, y Flyway apunta
cuáles ya corrió. Para cambiar algo **no se edita un archivo viejo: se crea el siguiente** (`V8__…`).
