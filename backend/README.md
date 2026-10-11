# Backend de Familias que Suman (primeros pasos)

Por ahora solo hay **base de datos** y **almacenamiento de fotos**, los dos en contenedores Docker.
Cada integrante los levanta en su propia computadora; lo que se comparte por Git son los archivos
(compose, migraciones y datos de prueba), no una base de datos común. Todavía no hay API.

## Levantarlo

Con Docker Desktop abierto, desde la raíz del repo:

```bash
git pull
cd backend
docker compose up -d
```

Eso es todo: crea la base, aplica las migraciones que falten, carga los datos de prueba y
levanta el almacenamiento de fotos. Después de cada `git pull`, el mismo `docker compose up -d`
te pone al día con los cambios de los demás. Los datos de prueba no se duplican.

Cuentas de prueba: `ana.rodriguez@correo.com` / `familia123` y `admin@correo.com` / `admin123`.

El `.env` es opcional: solo hace falta si quieres otros puertos o contraseñas (copia `.env.example` a `.env`).

Para ver las tablas:

```bash
docker compose exec db psql -U familias -d familias -c "\dt"
```

Para apagarlo: `docker compose down` (conserva los datos) o `docker compose down -v` (los borra y empiezas de cero).

## Qué es cada cosa

| Carpeta o archivo | Qué es |
|---|---|
| `docker-compose.yml` | Lista de contenedores que se levantan: la base de datos, el que crea las tablas y el de fotos. |
| `db/migrations/` | Archivos SQL que **crean y cambian las tablas**, en orden: `V1`, `V2`, `V3`... |
| `db/seed/dev.sql` | **Datos de prueba** para desarrollar. Se cargan solos al levantar. |
| `.env.example` | Plantilla opcional de contraseñas y puertos. Se copia a `.env` si quieres cambiarlos. |

**Migraciones, en una frase:** cada archivo cambia la base una sola vez, en orden, y Flyway (el contenedor
`migrar`) apunta cuáles ya corrió. Por eso, para cambiar algo, **no se edita un archivo viejo: se crea el siguiente**.

## Las migraciones que hay

| Archivo | Qué agrega |
|---|---|
| `V1__esquema_inicial.sql` | `cuentas`, `asociaciones`, `actividades`, `inscripciones` |
| `V2__familia.sql` | Foto de perfil en `cuentas`, `acompanantes`, `favoritos`, `preferencias` |
| `V3__participacion.sql` | `asistencias` (historial), `testimonios`, `respuestas_encuesta`, `invitaciones` |
| `V4__catalogo.sql` | `proyectos`, `formas_apoyo`, `centros`, `campanas`, `articulos_meta`, `aportaciones` |
| `V5__sugerencias_y_privacidad.sql` | `sugerencias`, `solicitudes_eliminacion` |

Ejemplo para agregar algo: para que las cuentas tengan teléfono, se crea `db/migrations/V6__telefono_en_cuentas.sql` con

```sql
ALTER TABLE cuentas ADD COLUMN telefono text;
```

y con `docker compose up -d` se aplica. Quien haga `git pull` y corra lo mismo, lo recibe.

## Si algo falla

- **"port is already allocated":** otra cosa usa ese puerto. Copia `.env.example` a `.env` y cambia
  `DB_PORT`, `STORAGE_API_PORT` o `STORAGE_CONSOLE_PORT`.
- **`migrar` dice "checksum mismatch":** se editó una migración que tu base ya había aplicado. Deshaz
  ese cambio (`git checkout` del archivo) o empieza de cero con `docker compose down -v`.
- **Quiero empezar de cero:** `docker compose down -v` y luego `docker compose up -d`.

## Seguridad

Los puertos solo escuchan en `127.0.0.1`: nada es visible desde la red. El `.env` y las contraseñas reales no se suben a Git.

## Siguientes pasos

- Decidir la API y agregarla al compose; las tablas ya cubren lo que hace la app.
- Crear el bucket de fotos (perfil y testimonios) cuando la API las suba.
