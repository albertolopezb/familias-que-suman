# 4. Evidencia de los controles de seguridad

Cada punto de la rúbrica (parte 2) con el control que lo cumple, dónde está en el código y cómo se
demuestra. El detalle técnico completo, con amenazas y matrices OWASP, está en la guía del backend:
[`docs/guia-backend.md`](../guia-backend.md) §4, §7 y §8.

**Demostración en vivo (2 min):** `bash backend/smoke.sh https://<túnel>`. Recorre los 35 casos,
incluidos los rechazos (SQLi, token alterado, otra familia, admin, fuerza bruta), contra el servidor
desplegado. Salida guardada en [`docs/evidencia/smoke.txt`](../evidencia/smoke.txt).

## 4.1 Controles en la API

| Lo que pide la rúbrica | Control implementado | Dónde | Cómo se demuestra |
|---|---|---|---|
| Sesiones e identidad robustas; secretos nunca en claro | JWT HS256 de 15 min con `iss`, `aud`, `exp`, `iat`, `jti` obligatorios; refresh token de 30 días rotado y de un solo uso (reusarlo cierra toda la sesión); cerrar sesión revoca en el servidor (Redis); contraseñas con **Argon2id**; código de recuperación guardado como HMAC, 15 min, 5 intentos; en el teléfono los tokens van cifrados con **AES-256-GCM** del Android Keystore | `identidad.py`, `routers/sesion.py`, `ServicioSesion.kt` | SEG-01, SEG-02; `smoke.sh` 4-13, 26-32 |
| Cada rol ve y hace solo lo suyo (IDOR/BOLA) | ABAC con Oso: cada regla en `politicas.polar` ("negar por omisión"); lo ajeno responde **404** (no revela que existe); permisos separados por función (`administrar:catalogo`, `moderar:contenido`, `registrar:asistencias`, `atender:privacidad`) | `politicas.polar`, `politicas.py`, `routers/*` | SEG-03, SEG-04; `smoke.sh` 14-19 |
| Acciones que modifican o borran con validación y privilegios | Cada ruta de `/v1/admin` exige su permiso; el cuerpo no puede traer permisos ni rol (mass assignment → 422); cambiar permisos invalida los tokens viejos; borrar con historial responde 409; cada acción sensible queda en la tabla `auditoria` (con huella HMAC de la IP); en la app, confirmación antes de borrar | `routers/admin.py`, `bitacora.py` | `test_autorizacion.py`, `test_politicas.py` |
| Consultas parametrizadas y validación estricta | SQLAlchemy (nunca SQL armado con texto); Pydantic con `extra="forbid"`, topes de longitud, rangos, rechazo de caracteres de control, enlaces solo `https:`/`tel:`/`mailto:`; la API entra a PostgreSQL con un usuario sin DDL | `esquemas.py`, `modelos.py`, `afterMigrate.sql` | SEG-05; `test_entradas.py` (23), `test_minimo_privilegio.py` (10); `smoke.sh` 20-21; ZAP reglas 40018-40022 sin hallazgos |
| Rate limiting y bloqueo temporal en inicio de sesión y registro | Iniciar sesión: 10/min y 60/h por IP; registro: 10/h por IP; recuperar: 5/h; límite global por IP; 5 contraseñas equivocadas bloquean ese correo 15 min venga de la IP que venga; contadores en Redis con ventana deslizante | `limites.py`, `identidad.py` | SEG-06; `smoke.sh` 35 |
| Tráfico cifrado y cabeceras de seguridad | Teléfono ↔ Cloudflare con TLS 1.2/1.3 (la app rechaza versiones viejas y HTTP en claro: `seguridad_red.xml`, `RESTRICTED_TLS`); cabeceras en cada respuesta: `Strict-Transport-Security`, `X-Content-Type-Options: nosniff`, `Content-Security-Policy: default-src 'none'`, `X-Frame-Options: DENY`, `Referrer-Policy`, `Cache-Control: no-store` | `seguridad.py`, `res/xml/seguridad_red.xml` | SEG-07; `smoke.sh` 22-24; `curl -I` |

## 4.2 Infraestructura y despliegue seguro

| Lo que pide la rúbrica | Cómo quedó |
|---|---|
| Contenedores con aislamiento y secretos por variables de entorno | Todo en Docker Compose. Contenedores sin capacidades de Linux (`cap_drop: ALL`), `no-new-privileges`, límites de memoria y procesos, la API con sistema de archivos de solo lectura. Tres redes: `borde` (túnel y API), `datos` (interna, sin internet: base, Redis, almacén) y `monitoreo` (interna). La API recibe solo sus variables (no tiene la contraseña de dueño de la base). Secretos en `backend/.env`, fuera de Git; con `ENTORNO=produccion` la API **se niega a arrancar** si encuentra un secreto de ejemplo |
| Expuesto por un túnel y consumido por la app | `cloudflared` sale hacia Cloudflare (sin abrir puertos). La API solo cree el encabezado `CF-Connecting-IP` si viene de la IP fija del túnel. La app de release habla solo HTTPS con esa dirección |
| Servidor endurecido | VM de Azure con dos firewalls en "negar por omisión", como el firewall *stateful* de la sesión 24: el grupo de seguridad de red de Azure y `ufw` en la VM. Solo entra SSH (22) con llave, sin contraseñas; ningún puerto de la API, la base ni Grafana queda expuesto. Para enseñar Grafana en la presentación se abre un segundo túnel de salida, solo a Grafana y con su usuario y contraseña, que se quita al terminar. Ver [`06-manual-instalacion.md`](06-manual-instalacion.md) §6.3 |

## 4.3 Validación de seguridad

| Caso | Qué responde la API | Evidencia |
|---|---|---|
| Inyección SQL en un filtro y en un id | 422, sin consultar la base | `smoke.sh` 20-21, `test_entradas.py::test_sqli_en_filtros_de_busqueda` |
| Token alterado (sin volver a firmar) | 401 | `smoke.sh` 12, `test_identidad.py::test_contenido_alterado_rompe_la_firma` |
| Token vencido | 401 `token_expirado` | `test_identidad.py::test_token_vencido` |
| `alg: none` u otro algoritmo | 401 | `::test_alg_none_se_rechaza`, `::test_otro_algoritmo_con_el_mismo_secreto_se_rechaza` |
| Refresh token reusado | 401 y se cierra toda la sesión | `smoke.sh` 27-28 |
| Consultar o borrar datos de otra familia | 404 | `smoke.sh` 18, `test_autorizacion.py::test_bola_no_se_ven_ni_se_borran_acompanantes_ajenos` |
| Familia entrando a funciones de admin | 403 | `smoke.sh` 14 |
| Registrarse como admin (campo extra) | 422 | `smoke.sh` 7 |
| Fuerza bruta contra un correo | 429 tras 5 intentos, aunque la contraseña sea buena | `smoke.sh` 35 |
| Exceso de peticiones | 429 con `Retry-After` | `test_seguridad_http.py::test_limite_global_por_ip` |
| Foto con HTML disfrazado, políglota o bomba de descompresión | 422 / se limpia | `test_fotos.py` |

**DAST con OWASP ZAP 2.17** (detalle en la guía §13). Dos pasadas: sin sesión y con el token de una
cuenta de escaneo sin datos reales.

| Riesgo | Alertas |
|---|---|
| Alto | 0 |
| Medio | 0 |
| Bajo | 0 |
| Informativo | 1 (*User Agent Fuzzer*) |

La alerta informativa no es una vulnerabilidad. ZAP compara respuestas con distintos `User-Agent`;
la API no lo lee, y lo que cambia es el campo `solicitud`, único en cada respuesta de error. Reporte:
[`backend/seguridad/zap/reporte-zap.html`](../../backend/seguridad/zap/reporte-zap.html).
