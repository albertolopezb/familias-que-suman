# 8. Administración final del proyecto

Plan en Planner: **Reto TC2007B.402** (exportado el 15 de octubre de 2026 y actualizado para esta
entrega). Captura del tablero final en la sección 10.

## 8.1 Backlog final y estado de los requerimientos

| Tipo | Total | Terminados | Parciales | Fuera |
|---|---|---|---|---|
| Requerimientos funcionales (RF-01 a RF-28) | 28 | 28 | 0 | 0 |
| Compromiso del acta: fichas sin conexión (ACTA-01) | 1 | 1 | 0 | 0 |
| Requerimientos no funcionales (RNF-01 a RNF-07) | 7 | 5 (RNF-01, 02, 04, 06 y 07) | 2: RNF-03 (cumple en el código; la rotación se demuestra en la presentación) y RNF-05 (no se probó en Android 10 exacto) | 0 |
| Historias de usuario (HU-01 a HU-14) | 14 | 14 | 0 | 0 |
| Tareas técnicas (TT-01 a TT-16) | 16 | 16 | 0 | 0 |

El estado de cada uno, con su prueba, está en la matriz de trazabilidad (§3). RNF-01 y RNF-07 se
midieron el 20 de octubre con el APK de entrega en un Samsung con Android 16, contra el servidor de
Azure: arranque en frío de 117 ms en promedio (meta 1.5 s) y confirmación de inscripción en ~0.2 s
(meta 2 s).

**Ajustes al backlog durante el proyecto:**

- **RF-16 y TT-13:** decían "push con FCM". Se implementó con WorkManager, sin Firebase (ver 8.3).
- **RF-17:** pasó de lista estática a favoritos que avisan de lo nuevo, a pedido de la socia.
- **RF-02:** el filtro por ciudad se mantiene. "Cerca de mí" con la ubicación aproximada se cambió
  (20 oct.) por "Buscar cerca de" en el mapa: la familia escribe un lugar o toca un punto, y la app
  ya no pide permiso de ubicación. El mapa ahora incluye los centros del Directorio.
- **Nuevos, salidos de la revisión final contra el acta y la rúbrica:**
  - ACTA-01: consulta sin conexión con Room.
  - Lista de "quiénes van" para la asociación: completa RF-06.
  - Pantalla para atender solicitudes de eliminación: completa RF-15 y HU-14.
  - Foto con cámara.

**Quedó fuera (y se dice):**

- iOS.
- Pagos.
- Mensajería entre familias.
- Inscribirse o donar sin conexión.
- Notificaciones instantáneas por FCM.
- Correo real en producción (hoy Mailpit; basta configurar `SMTP_*`).

## 8.2 Bitácora de hitos

| Hito | Fecha prevista | Fecha real | Cumplimiento |
|---|---|---|---|
| Establecimiento de requerimientos | 19/08/2026 | 20/08/2026 | 1 día tarde |
| Casos de uso y prototipo (Figma) | 31/08/2026 | 24/08/2026 | A tiempo |
| Esqueleto de la app (tema, dominio, navegación) | — | 21/09/2026 | — |
| RF imprescindibles e importantes (RF-01 a RF-11, RF-17 a RF-21) | 14/09/2026 | 01/10/2026 | 17 días tarde |
| **Entrega 2:** app funcional y backlog | 01/10/2026 | 01/10/2026 | A tiempo, con datos simulados en memoria |
| RF de la segunda ola (RF-12 a RF-16, RF-22, RF-23) | 08/10/2026 | 05/10/2026 | A tiempo (en la app; el backend llegó después) |
| Base de datos en Docker | 08/10/2026 | 07/10/2026 | A tiempo |
| Backend completo (API, monitoreo, túnel) | — | 13/10/2026 | No estaba planeado como hito |
| Identidad propia con JWT y ABAC (se quita Auth0) | — | 15/10/2026 | Cambio de rumbo (8.3) |
| Refresh tokens, revisión de testimonios, recuperación por correo, avisos del servidor, ZAP con sesión | — | 16/10/2026 | — |
| Cierre de pendientes del backlog (RNF-02, TT-15, TT-16) | 08/10/2026 | 16/10/2026 | 8 días tarde |
| Sin conexión, cámara, "quiénes van", eliminación desde la app, documentación final y despliegue en Azure | 22/10/2026 | 19 al 21/10/2026 | A tiempo |
| **Entrega 3** y presentación final | 22/10/2026 | 22/10/2026 | — |

## 8.3 Desviaciones entre lo planeado y lo ejecutado

| Desviación | Causa | Efecto y cómo se resolvió |
|---|---|---|
| La Entrega 2 se cerró con datos en memoria, sin backend | Se priorizó enseñar la experiencia completa a la socia | La integración, el cupo concurrente y la seguridad se concentraron en la última semana y media |
| Identidad: Auth0 del 13 al 15 de octubre y luego JWT propio | Se eligió Auth0 por el diagrama del profesor antes de leer el anexo de identidad del curso y la guía s23, que piden emitir los tokens propios | Dos días de retrabajo (migración V7, app, pruebas, guía). A cambio, refresh tokens rotados y bloqueo por correo propios |
| Notificaciones con WorkManager en lugar de FCM (RF-16, TT-13) | FCM pide cuenta de Google, Play Services y otra llave en el servidor, y obliga a declarar a Google en el aviso de privacidad; la Práctica 8 enseña a hacerlo sin Firebase | Los avisos llegan con hasta ~15 min de retraso. Aceptado y documentado |
| Favoritos dinámicos (RF-17) | La socia cambió el concepto: debía avisar de lo nuevo | Se reabrió RF-17 y se conectó con RF-22 |
| Plataformas: de Android e iOS a solo Android | Tiempo del reto y lo visto en el curso | Acordado con la socia |
| Ajustes pedidos por la socia (duplicar eventos, estilos, "Donar" en lugar de "Apartar") | Revisiones con la socia | Tareas To-do 2 a 5 en Planner, completadas del 5 al 7 de octubre |
| Servidor en Azure for Students en lugar de Oracle Cloud | Oracle rechazó la tarjeta en dos cuentas; la suscripción de estudiante de Azure solo permite cinco regiones (ninguna en México) y el primer tamaño elegido no tenía capacidad | Un día de pruebas de cuentas y regiones. Quedó en North Central US con Standard_B2als_v2, y el aviso de privacidad se actualizó para declarar dónde está el servidor |
| Se agregaron pantallas que el backlog daba por hechas ("quiénes van", atender eliminaciones, fichas sin conexión) | La revisión final contra el acta y la rúbrica mostró que el servidor guardaba los datos pero nadie podía consultarlos desde la app | Implementadas con sus pruebas el 19 de octubre |

## 8.4 Riesgos

| Riesgo | Acción tomada | Resultado |
|---|---|---|
| Backend no listo a tiempo | Docker Compose desde el primer día del backend; pruebas automáticas contra servicios reales | Backend completo el 13/10 con 180+ pruebas; hoy 191 en verde |
| Contraseñas y roles de prueba en el código | RNF-02 y TT-15: contraseñas fuera del código, admin por permisos firmados en el token | Se corrigió el atajo `correo.contains("admin")` (guía §9.4); búsqueda de secretos sin hallazgos |
| Sobrecupo por solicitudes simultáneas | Reserva atómica con `SELECT … FOR UPDATE` | La prueba con 8 familias a la vez nunca sobrepasa el cupo; sin el candado entraban 5 en 3 lugares |
| Datos de menores (inscripciones, testimonios, fotos) | Consentimiento del tutor, revisión antes de publicar, fotos sin EXIF, lista de asistentes sin correos | Cada control con su prueba (§9) |
| Cambios de requerimientos de la socia | Preguntas registradas y backlog actualizado | Se absorbieron Favoritos y Donar |
| Gastar el crédito de estudiante antes de tiempo | VM de 2 vCPU (~37 USD/mes con disco e IP); apagarla (`az vm deallocate`) fuera de las demostraciones; borrar el grupo de recursos al terminar | Alcanza para más de 2 meses encendida; sin tarjeta no hay cobros |
| Disco lleno en la computadora que corre Docker | Cachés y herramientas movidas a otro disco; ZAP instalado en lugar de su imagen | Docker se cayó una vez (13/10) y se recuperó; el servidor de la demo ya no depende de esa computadora |
| La dirección del túnel cambia y deja al APK sin servidor | Servidor siempre encendido en Azure; procedimiento de recompilación documentado | Riesgo aceptado para la demo; con dominio propio se elimina (túnel con nombre) |
| Autoría de commits no refleja quién hizo qué | Evidencia de la distribución en Planner (8.5) | Ver retrospectiva |

## 8.5 Distribución del trabajo

El reparto se planeó y se siguió en Planner: cada requerimiento, historia y tarea tiene responsable.
Requerimientos y casos de uso se trabajaron entre los cinco.

| Integrante | Tareas en Planner | Responsabilidades (ID del backlog) |
|---|---|---|
| **Alberto López Barragán** (A00841178) | 20 | Directorio, fichas y contacto (RF-01, RF-03, RF-08), proyectos y campañas (RF-09), información para donar (RF-10), insignias (RF-14), aviso de privacidad y eliminación (RF-15, HU-14, TT-14), panel y CRUD de administración (HU-02, TT-05, TT-12), navegación (TT-02), Android 10+ (RNF-05) |
| **José Andrés Fajardo Martínez** (A00842793) | 20 | Cupo en tiempo real e inscripción con acompañantes (RF-05, RF-06, RF-25, HU-07, TT-11), testimonios con revisión (RF-12, HU-11), notificaciones y avisos (RF-16, RF-22, HU-13, TT-09, TT-13), métricas de impacto (RF-23), ViewModels (TT-07), rendimiento (RNF-01, RNF-07) |
| **Gael Alexander Zaragoza Torres** (A01385956) | 20 | Cuenta y sesión (RF-18, RF-24, HU-01, TT-01), perfil e historial (RF-11), favoritos (RF-17, RF-27, HU-10, TT-08), filtros y apartado en campañas (RF-20, RF-21, HU-05), centros de donación (HU-06), UiState (TT-06), navegación con Atrás (RNF-04) |
| **Bárbara Martínez Mijares** (A01723424) | 18 | Ciudad y "Cerca de mí" (RF-02, RF-26), actividades y detalle (RF-04, HU-03), calendario (RF-07, HU-08, TT-10), encabezado (HU-04), componentes de UI (TT-04), estados de pantalla (RNF-06), secretos fuera del código (RNF-02, TT-15) |
| **Damaris Paola Castrellón Carrillo** (A01234497) | 10 | Encuestas (RF-13, HU-12), cancelación hasta 12 h (RF-19, RF-28, HU-09), estado al rotar (RNF-03), modelo de dominio y datos de prueba (TT-03) |

**Sobre el historial de Git:** varias aportaciones se integraron desde la cuenta de Alberto o desde
una misma computadora, así que el número de commits y de líneas por autor no refleja quién hizo cada
parte. Por ejemplo, un integrante aparece con dos nombres distintos. La evidencia de la
distribución es Planner. El archivo `.mailmap` del repositorio agrupa las identidades de cada
persona para que `git shortlog` sea legible.

## 8.6 Retrospectiva

**Qué funcionó**

1. **Probar contra los servicios de verdad.** Las pruebas del servidor corren contra PostgreSQL,
   Redis y el almacén reales, no contra simulaciones. Así aparecieron dos fallas que una prueba con
   simulaciones no habría visto:
   - el límite global de peticiones no frenaba nada por una incompatibilidad de slowapi con FastAPI
     0.143;
   - quitando el `FOR UPDATE`, entraban 5 familias en 3 lugares.
2. **Un estado de pantalla común (`UiState`) desde la Entrega 2.** Permitió trabajar las pantallas en
   paralelo con los mismos estados de cargando, vacío y error, y la integración con la API no obligó
   a rehacerlas.
3. **Diseñar primero los casos límite** (último lugar, meta agotada, sin conexión) en el prototipo.
   Cuando llegó el servidor, ya había pantalla para cada respuesta de error.
4. **Una bitácora de decisiones con fecha.** Cada vez que una decisión se reemplazó (Auth0 → JWT,
   testimonios sin revisión → con revisión), quedó escrito por qué. Eso sostuvo la defensa técnica.

**Qué no funcionó**

1. **Dejar el backend para el final.** La Entrega 2 con datos en memoria hizo que la integración, la
   seguridad y la concurrencia se juntaran en diez días. Algunos requisitos estaban "hechos" en la app
   pero el servidor no los respaldaba; por ejemplo, la asociación no tenía cómo ver la lista de
   asistentes.
2. **Elegir tecnología antes de leer los anexos del curso.** Auth0 se integró y se quitó en dos días
   porque el anexo de identidad pedía emitir tokens propios.
3. **Planner al final.** Los RNF, historias y tareas técnicas se capturaron casi al cierre, y las
   tareas del documento final seguían "sin iniciar" a una semana de la entrega. Planner dejó de
   servir para anticipar y se volvió un registro.
4. **Commits desde una cuenta compartida.** El historial no muestra el trabajo de cada quien.
5. **Atajos en el cliente.** El rol de administrador salió primero de "el correo contiene *admin*".
   Se corrigió, pero ningún proceso lo habría detectado antes de la revisión de seguridad.
6. **Construir antes de validar con la socia.** Favoritos se hizo estático y hubo que rehacerlo
   cuando ella explicó que debía avisar de lo nuevo.

**Qué cambiaríamos en un proyecto similar**

| Problema | Práctica nueva |
|---|---|
| Backend tardío | Desde la semana 1, un contrato OpenAPI y un servidor mínimo en Docker. La app consume la API real (aunque devuelva datos fijos) desde el primer sprint |
| Tecnología sin revisar | Antes de adoptar una herramienta externa, revisar los anexos y guías del curso y escribir la decisión en la bitácora con su alternativa |
| Planner como registro | Cada tarea nace con su criterio de aceptación y la prueba que la demostrará. "Terminado" = prueba en verde + fila en la matriz |
| Autoría invisible | Cada quien commitea con su cuenta y su correo, por *pull request* revisado por otra persona |
| Atajos de seguridad | Una revisión de seguridad por sprint (aunque sea corta), con OWASP ZAP y la lista de controles de la rúbrica |
| Validación tardía | Mostrar a la socia un prototipo navegable de cada función nueva antes de programarla |
| Dirección del servidor que cambia | Conseguir un dominio y un túnel con nombre al inicio, no al final |
