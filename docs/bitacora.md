# Bitácora del equipo

Decisiones de diseño y de arquitectura, con fecha y quién las tomó.

| Fecha | Decisión | Por qué |
|---|---|---|
| | Android nativo con Kotlin + Jetpack Compose, API 29+ | |
| | PostgreSQL como base de datos | |
| | Un solo repositorio: app en la raíz, backend en `backend/` | Un clone, un historial |
| | Cada integrante levanta el backend en su computadora con `docker compose`; se comparte el código (compose, migraciones y seed) por Git, no una base de datos común | Nadie depende de que la computadora de otra persona esté prendida, no se expone Postgres a la red y los cambios de esquema pasan por PR |
| | El esquema se versiona con migraciones Flyway (`backend/db/migrations`), en un contenedor | Es independiente del lenguaje que se elija para la API y deja todos los cambios a la base en el historial de Git |
| | Los testimonios se publican al enviarse y se pueden eliminar (su autora o autor y el admin) | Sin aprobación previa; la moderación es borrar lo que haga falta |
