# Backend de Familias que Suman

Todo corre en contenedores con Docker. **Cada integrante levanta su propia copia en su computadora**;
lo que se comparte entre el equipo es el código (compose, migraciones y datos de prueba) por Git,
no una base de datos común.

| Servicio | Qué es | En tu máquina |
|---|---|---|
| `db` | PostgreSQL 16 | `127.0.0.1:5432` |
| `migrar` | Flyway: crea y actualiza las tablas a partir de `db/migrations/`. Corre y termina. | — |
| `almacenamiento` | Fotos de testimonios, compatible con S3 (MinIO) | API `127.0.0.1:9000`, consola `127.0.0.1:9001` |
| `seed` | Datos de prueba (solo si lo pides) | — |

Todavía no hay API: está pendiente decidir si es propia o un BFF sobre el backend existente
(ver `docs/arquitectura-simple.drawio`).

## Primeros pasos

Necesitas Docker Desktop abierto.

```bash
cd backend
cp .env.example .env          # una sola vez; el .env es tuyo y no se sube a Git
docker compose up -d          # levanta db, aplica migraciones y levanta el almacenamiento
docker compose --profile seed run --rm seed   # opcional: datos de prueba
```

Cuentas de prueba del seed: `ana.rodriguez@correo.com` / `familia123` y `admin@correo.com` / `admin123`.

## Día a día

```bash
git pull && docker compose up -d      # después de traer cambios de otros: aplica sus migraciones
docker compose ps                     # qué está corriendo
docker compose logs -f db             # ver los mensajes de un servicio
docker compose down                   # apagar (conserva tus datos)
docker compose down -v                # apagar y BORRAR tus datos: la próxima vez empiezas de cero
```

Conectarte a la base con DBeaver, DataGrip, etc.: host `127.0.0.1`, puerto `DB_PORT`, y la base,
el usuario y la contraseña que pusiste en tu `.env`. O desde la terminal:

```bash
docker compose exec db psql -U familias -d familias
```

## Cambiar la base de datos

El esquema son archivos SQL en `db/migrations/`, con el nombre `V<número>__<qué cambia>.sql`
(`V2__agrega_telefono_a_cuentas.sql`). Flyway los aplica en orden y apunta en la tabla
`flyway_schema_history` cuáles ya corrió.

1. Crea el siguiente archivo (si el último es `V1`, el tuyo es `V2`) con **solo** lo que cambia:
   `ALTER TABLE`, `CREATE TABLE`, etc.
2. `docker compose up -d` lo aplica en tu base.
3. Sube la rama y haz PR como con cualquier código. Quien la reciba, con `docker compose up -d` la aplica.

Reglas:

- **Nunca edites una migración que ya está en `main`.** Flyway compara su huella y se niega a
  continuar. Un error en una migración vieja se corrige con una migración nueva.
- Si aún no la has subido y te equivocaste, edítala y corre `docker compose down -v && docker compose up -d`.
- Si dos personas crean `V2` en ramas distintas, la segunda en entrar a `main` renombra la suya a `V3`.
- No uses `ON DELETE CASCADE` en tablas de constancia (por ejemplo `solicitudes_eliminacion`).
- Los datos de prueba van en `db/seed/dev.sql`, nunca en una migración (las migraciones también corren en el servidor real).

## Cambiar el Docker

Se hace como código: rama, cambio en `docker-compose.yml` (o en una imagen), PR. Al hacer `git pull`,
el resto corre `docker compose up -d` y recibe el cambio. Cosas a cuidar:

- Imágenes con versión fija (`postgres:16`, `flyway/flyway:11`), no `latest`, para que todos tengan lo mismo.
  La excepción temporal es `almacenamiento` (ver el comentario en el compose).
- Una variable nueva va en `.env.example` con un valor de ejemplo, y se avisa al equipo para que la copie a su `.env`.
- Los secretos reales nunca van en el repo.

## Si algo no arranca

- **"port is already allocated"**: otra cosa usa ese puerto. Cambia `DB_PORT`, `STORAGE_API_PORT` o
  `STORAGE_CONSOLE_PORT` en **tu** `.env` y vuelve a correr `docker compose up -d`.
- **`migrar` falla con "checksum mismatch"**: alguien editó una migración que ya se había aplicado.
  Revísalo con quien la cambió; en tu máquina se arregla con `docker compose down -v`.
- **Otra persona no ve tus datos**: es lo esperado. Cada quien tiene su base; lo común es el esquema y el seed.

## Seguridad

- Los puertos solo escuchan en `127.0.0.1`: nada de lo que levantas aquí es visible desde la red ni desde internet.
  No lo cambies a `0.0.0.0`.
- El `.env` y cualquier contraseña real quedan fuera de Git.
- Las contraseñas de `.env.example` son de ejemplo; un servidor real usa otras, largas y distintas.

## Pendiente

- Decidir la API (propia o BFF) y agregarla como servicio `api`.
- Crear el *bucket* de fotos de testimonios cuando exista la API que lo use.
- Confirmar que la imagen del almacenamiento siga mantenida, o cambiarla por otra compatible con S3.
- Servidor compartido para pruebas del equipo, cuando se decida dónde se hospeda.
