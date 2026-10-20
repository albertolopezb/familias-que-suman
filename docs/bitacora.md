# Bitácora del equipo

Decisiones de diseño y de arquitectura, con fecha y quién las tomó.

| Fecha | Decisión | Por qué |
|---|---|---|
| | Android nativo con Kotlin + Jetpack Compose, API 29+ | |
| | PostgreSQL como base de datos | |
| | Un solo repositorio: app en la raíz, backend en `backend/` | Un clone, un historial |
| | Cada integrante levanta el backend en su computadora con `docker compose`; se comparte el código (compose, migraciones y seed) por Git, no una base de datos común | Nadie depende de que la computadora de otra persona esté prendida, no se expone Postgres a la red y los cambios de esquema pasan por PR |
| | El esquema se versiona con migraciones Flyway (`backend/db/migrations`), en un contenedor | Es independiente del lenguaje que se elija para la API y deja todos los cambios a la base en el historial de Git |
| | ~~Los testimonios se publican al enviarse~~ (reemplazada el 2026-10-16: ahora pasan por revisión) | Sin aprobación previa; la moderación era borrar lo que hiciera falta |
| 2026-10-13 | API propia con FastAPI (Python 3.13), SQLAlchemy y Pydantic | Lo que se vio en clase (s10, s22) y en las prácticas; validación y documentación OpenAPI automáticas |
| 2026-10-13 | Identidad con Auth0: Universal Login + PKCE, JWT RS256, permisos por rol | Lo pide el diagrama del profesor; las contraseñas y la fuerza bruta las protege un servicio especializado; con RS256 la API solo verifica |
| 2026-10-13 | Las contraseñas salen de nuestra base (V6) y de la app | Un dato que no se guarda no se puede robar |
| 2026-10-13 | Autorización ABAC con las políticas en `politicas.py` | Las reglas dependen de de quién es y de cuándo, no solo del rol |
| 2026-10-13 | RustFS en lugar de MinIO para las fotos | La imagen comunitaria de MinIO se archivó y ya no recibe parches |
| 2026-10-13 | Redis para rate limit y sesiones cerradas | Los límites sobreviven reinicios y el cierre de sesión es inmediato |
| 2026-10-13 | Cloudflare Tunnel para salir a internet | HTTPS sin abrir puertos; la red de Cloudflare absorbe ataques de red |
| 2026-10-13 | Prometheus + Grafana con alertas de seguridad | OWASP A09: detectar, no solo prevenir |
| 2026-10-13 | OWASP ZAP en cada entrega (0 altas, 0 medias, 0 bajas) | Comprobar desde afuera lo que dicen las pruebas |
| 2026-10-15 | Se quita Auth0: la API emite y verifica sus propios JWT (HS256) con `POST /v1/sesion/iniciar` | Es lo que pide la guía de clase (s23): identificación y autorización con JWT, sin un servicio externo que configurar; reemplaza la fila de Auth0 del 13 |
| 2026-10-15 | Las contraseñas vuelven a la base, solo como hash Argon2id (V7), con bloqueo por fuerza bruta | Sin Auth0 alguien tiene que guardarlas; Argon2id es la primera opción de OWASP y el bloqueo por correo sustituye al de Auth0 |
| 2026-10-15 | Las políticas ABAC pasan a Polar (`politicas.polar`) evaluadas por Oso; la API baja a Python 3.12 | Como en la guía (s23 §4.2): reglas declarativas fuera del código de las rutas; Oso solo publica paquetes hasta 3.12 |
| 2026-10-15 | Los permisos de admin viven en la cuenta (`permisos`) y viajan en el JWT; cambiarlos invalida los tokens viejos | Los atributos del sujeto van firmados en el token (la guía), sin dejar que un token viejo conserve permisos quitados |
| 2026-10-16 | Access token de 15 min + refresh token de 30 días, rotado y de un solo uso (V8, tabla `sesiones`); reusar uno viejo cierra toda la sesión | Un JWT robado ya no sirve un día entero y la familia no escribe su contraseña cada 15 minutos; la detección de reuso descubre un refresh robado. Reemplaza el token de 24 h del 15 |
| 2026-10-16 | Los testimonios pasan por revisión antes de publicarse (`pendiente` → `aprobado`/`rechazado`); lo revisa quien tiene `moderar:contenido` | Lo pide el backlog (RF-12 / HU-11: "nada se publica sin que Familias que Suman lo haya aprobado"). Reemplaza la fila sin fecha de "se publican al enviarse" |
| 2026-10-16 | Cancelar una inscripción solo hasta 12 horas antes (`fuera_de_plazo`, 409) | RF-19 del backlog: la asociación necesita saber con tiempo cuántas personas llegan |
| 2026-10-16 | "Cerca de mí" (RF-02) con ubicación aproximada, solo en el teléfono: no se manda a la API ni se guarda | Decisión del equipo por privacidad y ética: para ordenar una lista basta la distancia aproximada; un dato que no se recibe no se puede filtrar |
| 2026-10-16 | Aviso de privacidad reescrito conforme a la LFPDPPP (DOF 20-03-2025), arts. 15 y 16: integral en Ajustes y simplificado al crear la cuenta | El anterior no tenía responsable ni domicilio, ni lista completa de datos, ni plazos, y prometía un correo que no existe. Responsable, domicilio y correo (hola@familiasquesuman.com) tomados del aviso oficial del sitio, que todavía no publica la razón social ni el domicilio completo (`Responsable` en `AvisoPrivacidadScreen.kt`) |
| 2026-10-16 | Al eliminar una cuenta, sus sugerencias se quedan sin nombre ni contacto | Las sugerencias son sobre causas, no sobre la familia; así no queda ningún dato suyo |
| 2026-10-16 | ~~"¿Olvidaste tu contraseña?" queda parcial (RF-18)~~ (reemplazada abajo el mismo día) | Se resolvió con Mailpit |
| 2026-10-16 | Recuperar la contraseña con un código de 6 dígitos por correo (V9): HMAC, 15 minutos, un solo uso, 5 intentos, 3 por hora; Mailpit como correo de desarrollo | El anexo del reto: "con correo, hay un token de un solo uso". Mailpit es un contenedor más y no depende de una cuenta externa; para producción basta cambiar `SMTP_*` |
| 2026-10-16 | Avisos del servidor (urgencias de la ciudad y actividades nuevas) que la app pide cada 15 minutos con WorkManager, sin Firebase | La Práctica 8 de startdroid lo hace así ("sin Firebase, sin Google"). FCM pediría una cuenta de Google, Play Services y una llave privada más en el servidor; el aviso de privacidad tendría que declarar a Google |
| 2026-10-16 | La app solo acepta TLS 1.2 y 1.3 con cifrados AEAD (`RESTRICTED_TLS`) | Al medir el TLS real se vio que el borde del Quick Tunnel todavía acepta TLS 1.1; con Quick Tunnel no se puede configurar, así que se cierra del lado del cliente |
| 2026-10-19 | Fichas sin conexión con Room: se guarda la última respuesta de cada ruta pública (actividades, asociaciones, proyectos, campañas, centros, ciudades) y sin red se enseña con la franja "Sin conexión · estás viendo lo guardado el…" y sin cupo | El acta de acuerdos compromete la consulta sin conexión; la Práctica 5 enseña Room. Solo lo público: los datos de la familia no se copian al teléfono |
| 2026-10-19 | Foto con la cámara con `TakePicture` + FileProvider, sin declarar `CAMERA` | Práctica 10: la app de cámara del sistema toma la foto y la app nunca ve la cámara en vivo; la foto pasa por la misma limpieza de EXIF |
| 2026-10-19 | "Quiénes van" (`GET /v1/admin/actividades/{id}/inscritos`) con el permiso `registrar:asistencias`, sin correos y auditado | RF-06 dice que la asociación recibe la lista; el servidor la guardaba pero nadie podía consultarla. Minimización: nombre y edad bastan para organizarse |
| 2026-10-19 | Atender solicitudes de eliminación desde la app (pantalla de administración) | El aviso de privacidad promete la eliminación: la rúbrica pide que sea demostrable |
| 2026-10-19 | Los botones de prueba (notificaciones de demostración, forzar estados, simular sin cupo) solo en depuración; en la entrega, los de notificaciones solo para la administración | Una familia no debe ver herramientas de prueba; la administración los necesita para la demostración |
| 2026-10-19 | Servidor de la demostración en una VM Oracle Cloud Always Free en Querétaro (`backend/despliegue/oracle.sh`), con secretos de producción generados en la VM y `ENTORNO=produccion` | Gratis y sin dormirse (a diferencia de Render), mismo `docker compose`, y los datos se quedan en México (sin transferencia internacional que declarar) |
