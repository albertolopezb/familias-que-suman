# 1. Resumen del alcance final y referencia al acta de acuerdos

**Familias que Suman** conecta a familias con asociaciones, actividades de voluntariado y campañas
de donación en Monterrey y Hermosillo. Ya tiene un sitio web. La socia formadora pidió que la app
aporte lo que el teléfono hace mejor:

- avisos que llegan solos (recordatorios y urgencias de la ciudad);
- contacto directo por WhatsApp, llamada y correo;
- una sesión que se queda abierta para consultar perfil, historial e insignias sin entrar cada vez.

La solución es una **app Android nativa** (Kotlin, Jetpack Compose) conectada a una **API propia**
(FastAPI y PostgreSQL en Docker). La API se publica en internet por un túnel HTTPS de Cloudflare
desde una VM de Microsoft Azure pagada con el crédito de Azure for Students (sin costo para el equipo).

## 1.1 Compromisos del acta de acuerdos y cómo se cumplieron

> Acta de acuerdos con la socia formadora: *[enlace al acta]*.
> Requerimientos del equipo: https://docs.google.com/document/d/1E9YGMQR1R2KuULSeeNgVbwip-9pn6ljhmjwBu2FMb7o

| Compromiso | Cómo quedó | Evidencia |
|---|---|---|
| Consulta de proyectos sociales activos, con filtrado por categoría y detalle | Pestañas Proyectos, Donar (filtros por categoría y urgencia) y Directorio, con ficha de cada uno | RF-09, RF-20 |
| Contacto directo por WhatsApp, llamada e Instagram | Botones en fichas de asociación, proyecto y centro que abren la app correspondiente con el dato puesto | RF-08 |
| Consulta de fichas de proyectos y actividades sin conexión | El catálogo público se guarda en el teléfono (Room); sin internet se ve lo guardado, con aviso de la fecha y sin presumir cupo | ACTA-01, `CatalogoLocalTest` |
| Comunicación cifrada HTTPS/TLS con autenticación por tokens JWT | TLS 1.2/1.3 en todo el camino público; JWT propio de 15 min con refresh rotado | SEG-01, SEG-07 |

## 1.2 Qué entra

- App Android en español (Android 8 o superior; la meta del backlog es Android 10+).
- Directorio de asociaciones y centros por categoría, filtro por ciudad (Monterrey y Hermosillo),
  "Buscar cerca de" un lugar en el mapa (sin pedir la ubicación), fichas y contacto directo.
- Actividades con cupo en tiempo real, inscripción con acompañantes (consentimiento del tutor para
  menores), cancelación hasta 12 horas antes y "Agregar al calendario".
- Campañas de donación con filtros y apartado de artículos con meta en tiempo real. La donación es
  informativa: la app no procesa pagos.
- Cuenta con registro diferido (explorar no pide cuenta), recuperación de contraseña con código por
  correo y sesión persistente cifrada.
- Perfil, historial, favoritos que avisan de lo nuevo, insignias, métricas de impacto, testimonios
  con foto (galería o cámara) revisados antes de publicarse, encuestas antes y después, ajustes y
  aviso de privacidad con solicitud de eliminación.
- Recordatorio 24 h antes y avisos personalizados (urgencias de la ciudad, actividades nuevas) con la
  app cerrada.
- Administración en la app, según permisos:
  - crear, editar, duplicar y borrar actividades, proyectos, campañas, centros y asociaciones;
  - revisar testimonios y sugerencias;
  - enviar avisos;
  - ver quiénes van a cada actividad;
  - atender solicitudes de eliminación de datos.
- Backend propio en contenedores, con monitoreo (Prometheus y Grafana), pruebas automáticas y
  escaneo OWASP ZAP.

## 1.3 Qué no entra

- Pagos o donativos en línea.
- Mensajería entre familias: el único canal es familia ↔ asociación.
- iOS.
- Rastreo de ubicación. La app no pide la ubicación: "Buscar cerca de" usa un lugar que la familia
  escribe o toca en el mapa.
- Modo sin conexión completo: sin internet se **consulta** lo guardado. Inscribirse, apartar y todo
  lo que cambia algo requiere conexión, porque el cupo y las metas se deciden en el servidor.
- Notificaciones instantáneas por Firebase: los avisos llegan con WorkManager, con hasta ~15 min de
  retraso.
