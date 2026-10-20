# Entrega 3 · Familias que Suman (Equipo 6, TC2007B.402)

Las secciones del documento final, en el orden que pide la rúbrica. El PDF que se sube a Canvas se
arma con estas secciones; aquí viven versionadas junto al código que describen.

| # | Sección | Archivo |
|---|---|---|
| 1 | Resumen del alcance final y referencia al acta | [01-alcance.md](01-alcance.md) |
| 2 | Arquitectura final (C4) y decisiones técnicas | [02-arquitectura.md](02-arquitectura.md) |
| 3 | Matriz de trazabilidad y evidencia de pruebas | [03-matriz-trazabilidad.md](03-matriz-trazabilidad.md) |
| 4 | Evidencia de los controles de seguridad | [04-seguridad.md](04-seguridad.md) |
| 5 | Manual de usuario | [05-manual-usuario.md](05-manual-usuario.md) |
| 6 | Manual de instalación | [06-manual-instalacion.md](06-manual-instalacion.md) |
| 7 | Plan de implantación | [07-plan-implantacion.md](07-plan-implantacion.md) |
| 8 | Administración final: backlog, hitos, desviaciones, riesgos, distribución y retrospectiva | [08-administracion.md](08-administracion.md) |
| 9 | Consideraciones éticas y legales (criterios verificables, aviso, clasificación, dilemas) | [09-etica-legal.md](09-etica-legal.md) |
| 10 | Enlaces de la entrega | [10-enlaces.md](10-enlaces.md) |

## Evidencia

| Archivo | Qué es |
|---|---|
| [`../evidencia/pytest-servidor.txt`](../evidencia/pytest-servidor.txt) | Las 182 pruebas del servidor, una por línea |
| [`../evidencia/pruebas-app.txt`](../evidencia/pruebas-app.txt) | Las 60 pruebas locales de la app |
| [`../evidencia/pruebas-instrumentadas.txt`](../evidencia/pruebas-instrumentadas.txt) | Las 4 pruebas de interfaz, corridas en el teléfono físico |
| [`../evidencia/smoke.txt`](../evidencia/smoke.txt) | Prueba de contrato (35 casos) |
| [`../evidencia/secretos.txt`](../evidencia/secretos.txt) | Búsqueda de secretos en todo el historial (gitleaks) |
| [`../../backend/seguridad/zap/`](../../backend/seguridad/zap/) | Reportes de OWASP ZAP (HTML, Markdown, JSON) |

Referencias técnicas: [guía del backend](../guia-backend.md) y [bitácora de decisiones](../bitacora.md).
