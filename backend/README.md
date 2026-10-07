# Backend de Familias que Suman (primeros pasos)

Por ahora solo hay **base de datos** y **almacenamiento de fotos**, los dos en contenedores Docker.
Cada integrante los levanta en su propia computadora; lo que se comparte por Git son los archivos
(compose, migraciones y datos de prueba), no una base de datos común. Todavía no hay API.

## Levantarlo

Con Docker Desktop abierto:

```bash
cd backend
cp .env.example .env                           # una sola vez (el .env es tuyo, no se sube a Git)
docker compose up -d                           # levanta la base y crea las tablas
docker compose --profile seed run --rm seed    # opcional: datos de prueba
```

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
| `db/seed/dev.sql` | **Datos de prueba** para desarrollar. Solo se cargan si lo pides. |
| `.env.example` | Plantilla de las contraseñas y puertos. Se copia a `.env`. |

**Migraciones, en una frase:** cada archivo cambia la base una sola vez, en orden, y Flyway (el contenedor
`migrar`) apunta cuáles ya corrió. Por eso, para cambiar algo, **no se edita un archivo viejo: se crea el siguiente**.

Ejemplo: para que las cuentas tengan teléfono, se crea `db/migrations/V2__telefono_en_cuentas.sql` con

```sql
ALTER TABLE cuentas ADD COLUMN telefono text;
```

y con `docker compose up -d` se aplica. Quien haga `git pull` y corra lo mismo, lo recibe.

## Si algo falla

- **"port is already allocated":** otra cosa usa ese puerto. Cambia `DB_PORT`, `STORAGE_API_PORT` o
  `STORAGE_CONSOLE_PORT` en tu `.env`.
- **Quiero empezar de cero:** `docker compose down -v` y luego `docker compose up -d`.

## Seguridad

Los puertos solo escuchan en `127.0.0.1`: nada es visible desde la red. El `.env` y las contraseñas reales no se suben a Git.

## Siguientes pasos

- Agregar el resto de las tablas como `V2`, `V3`... (testimonios, encuestas, campañas, proyectos, favoritos).
- Decidir la API y agregarla al compose.
