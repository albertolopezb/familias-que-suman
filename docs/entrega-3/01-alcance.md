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

## 1.1 Acuerdos con el socio formador y cómo se cumplieron

**Sobre el acta de acuerdos.** No existe un acta independiente firmada con el socio formador. El
acuerdo de alcance quedó en dos documentos, que son los que esta entrega toma como referencia:

- **Requerimientos Iniciales Validados** (RF-01 a RF-15, con su columna «Decisión del socio
  formador»), el documento que el profesor publicó en Canvas.
- **Requerimientos Equipo 6**, la versión ampliada del equipo: RF-01 a RF-23, casos de uso,
  preguntas y respuestas con la representante del socio formador, historias de usuario y el enlace
  al backlog en Planner:
  https://docs.google.com/document/d/1E9YGMQR1R2KuULSeeNgVbwip-9pn6ljhmjwBu2FMb7o

RF-24 a RF-29 se agregaron al backlog durante el desarrollo; en la matriz (§3) se indica su origen.

**Lo que pidió el socio formador** (requerimientos validados y sesiones con su representante):

| Pedido | Cómo quedó | Evidencia |
|---|---|---|
| Consulta de proyectos y campañas activos, con filtrado por categoría y detalle | Pestañas Proyectos, Donar (filtros por categoría y urgencia) y Directorio, con ficha de cada uno | RF-09, RF-20 |
| Contacto directo con la asociación en un toque | Botones de llamada, WhatsApp, correo e Instagram en las fichas, que abren la app correspondiente con el dato puesto | RF-08 |
| Una app que aproveche el teléfono: avisos que llegan solos y una sesión que se queda abierta | Recordatorio 24 h antes y avisos de la ciudad con la app cerrada (WorkManager); sesión cifrada que se renueva sola | RF-16, RF-18, RF-22 |

**Lo que agregó el equipo** (no venía de los requerimientos del socio):

| Decisión | Cómo quedó | Evidencia |
|---|---|---|
| Consulta de fichas sin conexión | El catálogo público se guarda en el teléfono (Room); sin internet se ve lo guardado, con aviso de la fecha y sin presumir cupo | RF-29, `CatalogoLocalTest` |
| Comunicación cifrada HTTPS/TLS y autenticación con tokens JWT | Pedidas por la rúbrica de seguridad de la Entrega 3 y el curso: TLS 1.2/1.3 en todo el camino público; JWT propio de 15 min con refresh rotado | SEG-01, SEG-07 |

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
- Rastreo de ubicación y alertas por cercanía con GPS (*geofencing*). La app no pide la ubicación:
  "Buscar cerca de" usa un lugar que la familia escribe o toca en el mapa (dilema 1, §9.4).
- Modo sin conexión completo: sin internet se **consulta** lo guardado. Inscribirse, apartar y todo
  lo que cambia algo requiere conexión, porque el cupo y las metas se deciden en el servidor.
- Notificaciones instantáneas por Firebase: los avisos llegan con WorkManager, con hasta ~15 min de
  retraso.

## 1.4 Operación sin conexión y sincronización posterior

La rúbrica pide que, si hay operación sin conexión, la sincronización posterior evite duplicados o
pérdida de información. En esta entrega el modo sin conexión es de **solo lectura**: se guarda y se
consulta el catálogo público, y lo único que el teléfono escribe sin red es esa copia de lo ya
publicado, que la respuesta nueva del servidor reemplaza completa cuando vuelve la red.

Todo lo que cambia datos (inscribirse, cancelar, apartar artículos, testimonios) **requiere
conexión** y se decide en el servidor. Por eso **no hay cola de operaciones pendientes ni
sincronización posterior: no hay nada que pueda duplicarse o perderse al volver la red**. Y aun con
una red inestable, los reintentos no duplican: inscribirse es un `PUT` idempotente (la misma
petición dos veces deja una sola inscripción) y apartar artículos nunca pasa de lo que falta
(RF-21, `test_aportaciones.py`; RF-05 y RF-06, `test_inscripciones.py`).
