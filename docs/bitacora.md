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
| 2026-10-13 | API propia con FastAPI (Python 3.13), SQLAlchemy y Pydantic | Lo que se vio en clase (s10, s22) y en las prácticas; validación y documentación OpenAPI automáticas |
| 2026-10-13 | Identidad con Auth0: Universal Login + PKCE, JWT RS256, permisos por rol | Lo pide el diagrama del profesor; las contraseñas y la fuerza bruta las protege un servicio especializado; con RS256 la API solo verifica |
| 2026-10-13 | Las contraseñas salen de nuestra base (V6) y de la app | Un dato que no se guarda no se puede robar |
| 2026-10-13 | Autorización ABAC con las políticas en `politicas.py` | Las reglas dependen de de quién es y de cuándo, no solo del rol |
| 2026-10-13 | RustFS en lugar de MinIO para las fotos | La imagen comunitaria de MinIO se archivó y ya no recibe parches |
| 2026-10-13 | Redis para rate limit y sesiones cerradas | Los límites sobreviven reinicios y el cierre de sesión es inmediato |
| 2026-10-13 | Cloudflare Tunnel para salir a internet | HTTPS sin abrir puertos; la red de Cloudflare absorbe ataques de red |
| 2026-10-13 | Prometheus + Grafana con alertas de seguridad | OWASP A09: detectar, no solo prevenir |
| 2026-10-13 | OWASP ZAP en cada entrega (0 altas, 0 medias, 0 bajas) | Comprobar desde afuera lo que dicen las pruebas |
