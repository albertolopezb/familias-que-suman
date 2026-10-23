# 7. Plan de implantación

Cómo poner el sistema en operación con Familias que Suman, de la entrega del reto a su uso real.

## 7.1 Versión entregada

| Pieza | Versión |
|---|---|
| Repositorio | tag `entrega-final` |
| App Android | `versionName 3.2`, `versionCode 5`, firmada con la llave del equipo (SHA-256 `88:C7:BB:77:6B:BF:3C:49:3F:D3:38:CC:14:17:33:60:74:EB:65:52:BC:8E:88:45:EC:E6:AE:7C:E8:F2:87:0C`); Android 8.0+ (`minSdk 26`), `targetSdk 37` |
| API | FastAPI, Python 3.12, imagen construida del mismo tag |
| Esquema de la base | Flyway `V1` a `V11` |
| Servidor | VM de Microsoft Azure (Azure for Students), Standard_B2als_v2, Ubuntu 24.04, North Central US, con Docker Compose |

## 7.2 Canal de distribución

| Etapa | Canal | Por qué |
|---|---|---|
| Piloto (entrega del reto y primeras familias) | **APK firmado** descargable desde un enlace del equipo (Drive o la página de *Releases* de GitHub), instalado con "origen desconocido" | Sin costo ni revisión de tienda; permite probar con pocas familias |
| Operación | **Google Play**, con una cuenta de desarrollador **a nombre de Familias que Suman** (pago único de 25 USD), primero en *prueba interna* y luego en producción | Actualizaciones automáticas y confianza de las familias. La ficha de Play debe enlazar el mismo aviso de privacidad. Si la llave de firma pasa a Play App Signing, la huella nueva se agrega a `APP_HUELLAS_SHA256` |

La llave de firma y su contraseña se entregan a la OSF por un canal privado (no por correo ni por el
repositorio) y se guardan en dos lugares.

## 7.3 Configuración necesaria

| Qué | Dónde | Quién |
|---|---|---|
| Secretos de producción (base, Redis, almacén, `JWT_SECRETO`, `HMAC_SECRETO`, Grafana) al azar y distintos | `backend/.env` en el servidor (lo genera `desplegar.sh`) | Equipo técnico |
| `ENTORNO=produccion` | `backend/.env` | `desplegar.sh` |
| Dirección pública HTTPS | Quick Tunnel (piloto) o túnel con nombre y dominio propio (operación) | Equipo técnico / OSF (dominio) |
| `api.url.release` de la app con esa dirección | `local.properties` al compilar | Equipo técnico |
| Correo real para recuperar contraseñas | `SMTP_HOST`, `SMTP_PUERTO`, `SMTP_USUARIO`, `SMTP_CONTRASENA`, `SMTP_TLS=true` | OSF (cuenta de correo transaccional) |
| Cuentas de administración | `python -m app.herramientas permisos <correo> <permiso>` | Equipo técnico, a pedido de la OSF |
| Responsable, domicilio y correo del aviso de privacidad | `Responsable` en `AvisoPrivacidadScreen.kt` | OSF (razón social y domicilio completos) |
| App Links con dominio propio | `enlaces.dominio` en la app y `ENLACES_BASE` en el servidor | Equipo técnico |

## 7.4 Procedimiento de despliegue

1. **Antes:**
   - Las 191 pruebas del servidor y las 66 de la app en verde.
   - ZAP sin altas ni medias.
   - Etiquetar el commit (`git tag vX.Y-…`).
2. **Respaldo** (si ya hay datos):
   ```bash
   ssh … 'cd ~/familias/backend && sudo docker compose exec -T db pg_dump -U familias -d familias -Fc' > respaldo-AAAA-MM-DD.dump
   ```
3. **Desplegar el servidor:** `bash backend/despliegue/desplegar.sh <IP> <usuario>`. Flyway aplica solo las
   migraciones nuevas; el `.env` y los volúmenes se conservan.
4. **Verificar:** `bash backend/smoke.sh https://<dirección>` (35 casos) y revisar en Grafana que no
   suban los 5xx ni los 401/403.
5. **Publicar la app:** compilar con la dirección final, subir el APK (o el AAB a Play) y avisar a las
   familias del piloto.
6. **Capacitación** al equipo de la OSF (1 sesión, con el §5.12 del manual): administrar el
   catálogo, revisar testimonios, enviar avisos, ver quiénes van y atender solicitudes de
   eliminación.
7. **Piloto de 2 semanas** con 5 a 10 familias y 2 actividades reales. Al terminar se revisan las
   encuestas y los incidentes antes de abrirlo a todas las familias.

## 7.5 Carga inicial de información

**Familias que Suman no tiene una base de datos previa que migrar.** Su información vive hoy en el
sitio web (familiasquesuman.com) y en hojas de cálculo y mensajes del equipo.

| Datos | Cómo se cargan |
|---|---|
| Esquema | Flyway lo crea solo al levantar (`backend/db/migrations`) |
| Ciudades (Monterrey, Hermosillo) | Migraciones |
| Asociaciones, proyectos, campañas, centros y actividades | El equipo de la OSF los captura desde la app con su cuenta de administración (§5.12). Para una carga grande, un script SQL con el formato de `backend/db/seed/dev.sql`, revisado y ejecutado por el equipo técnico |
| Fotos y logos | Se suben desde la administración; el servidor las re-codifica y les quita los metadatos |
| Familias | No se importan: cada familia crea su cuenta y acepta el aviso de privacidad (consentimiento) |
| Datos de prueba | En producción **no** deben quedar las cuentas de prueba. En el piloto se conservan con contraseñas al azar (`desplegar.sh`) y se borran al abrirlo a todas las familias |

## 7.6 Rollback (si la implantación falla)

| Señal | Decisión |
|---|---|
| `smoke.sh` falla, `/v1/salud` no responde o los 5xx suben en Grafana después de desplegar | Regresar el servidor a la versión anterior |
| La app nueva truena o no puede entrar | Publicar la versión anterior |

**Servidor:**

1. `git checkout <tag anterior>` y `bash backend/despliegue/desplegar.sh <IP> <usuario>`. Se reconstruye la
   API con el código anterior.
2. Si la versión nueva traía una **migración**, Flyway no deshace cambios. Se restaura el respaldo
   del paso 7.4-2:
   ```bash
   sudo docker compose exec -T db pg_restore -U familias -d familias --clean < respaldo-AAAA-MM-DD.dump
   ```
3. Verificar con `smoke.sh`.

Tiempo estimado: 15 minutos.

**App:** Android no deja instalar una versión con `versionCode` menor encima de una mayor. El rollback
es publicar el código anterior con un `versionCode` **nuevo y mayor**, firmado con la misma llave (en
Play, también se puede detener el lanzamiento escalonado).

**Si el servidor no está disponible** (se acaba el crédito de estudiante o la región se queda sin capacidad): el mismo
`docker compose` corre en cualquier máquina con Docker. Se restaura el último respaldo y se publica con
el túnel; la app solo necesita la nueva dirección.

## 7.7 Después de implantar

- **Respaldos:** `pg_dump` diario con cron en la VM; una copia semanal fuera del servidor.
- **Monitoreo:** alertas de Grafana (guía §11): fuerza bruta, picos de 403, entradas inválidas.
- **Privacidad:** atender las solicitudes de eliminación en el plazo legal (20 + 15 días) desde la app.
- **Actualizaciones:** dependencias cada mes (`uv lock --upgrade`, versiones de imágenes) con las
  pruebas y ZAP antes de desplegar.
