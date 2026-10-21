# Familias que Suman — App Android + API

App para que las familias de Monterrey y Hermosillo encuentren actividades de voluntariado, se
inscriban con sus hijos y apoyen campañas de donación de Familias que Suman. Reto TC2007B.402,
Equipo 6.

**App:** Kotlin · Jetpack Compose · Room · WorkManager · Android 8.0 (API 26) o superior.
**API:** FastAPI · PostgreSQL · Redis · RustFS · Docker Compose, publicada por un túnel de
Cloudflare.

## Entrega final

Todo lo de la entrega 3 está en [`docs/entrega-3/`](docs/entrega-3/README.md):

- alcance;
- arquitectura C4;
- matriz de trazabilidad;
- seguridad;
- manuales de usuario e instalación;
- plan de implantación;
- administración del proyecto;
- ética y legal.

## Correr el proyecto

```bash
git clone https://github.com/albertolopezb/familias-que-suman.git
```

```bash
cd familias-que-suman/backend && docker compose up -d --build
```

La API queda en http://localhost:8000/v1/salud. Luego, en Android Studio: **File → Open** → la
carpeta `familias-que-suman` → ▶ **Run** en un emulador. Configuración y APK firmado:
[manual de instalación](docs/entrega-3/06-manual-instalacion.md).

## Estructura

```
familias-que-suman/          ← se abre esta carpeta en Android Studio
├── app/                     ← el módulo Android
│   └── src/main/java/mx/tec/familiasquesuman/
│       ├── domain/          ← modelos y reglas, Kotlin puro
│       ├── data/            ← repositorios (API), sesión (JWT cifrado) y local/ (Room, sin conexión)
│       ├── notificaciones/  ← avisos con WorkManager
│       ├── ui/              ← pantallas, componentes, navegación, ViewModels y tema
│       ├── FamiliasApplication.kt
│       └── MainActivity.kt
├── backend/                 ← API, base, monitoreo, seguridad y despliegue (Docker). Gradle lo ignora
│   └── .env.example
├── docs/                    ← entrega 3, guía del backend, bitácora y evidencia de pruebas
└── gradle/  build.gradle.kts  settings.gradle.kts  gradlew
```

## Pruebas

| Qué | Comando | Resultado |
|---|---|---|
| Servidor (contra PostgreSQL, Redis y RustFS reales) | `cd backend && docker compose --profile pruebas run --rm --build pruebas` | 190 pasan |
| Contrato de la API | `bash backend/smoke.sh` | 35 de 35 |
| App (JVM) | `./gradlew :app:testDebugUnitTest` | 56 pasan |
| App (interfaz, en un dispositivo) | `./gradlew :app:connectedDebugAndroidTest` | 4 pruebas de Compose |
| OWASP ZAP (sin y con sesión) | `bash backend/seguridad/zap/escanear.sh` | 0 altas, 0 medias, 0 bajas |

## Documentación

- **Entrega final:** [docs/entrega-3](docs/entrega-3/README.md)
- **Backend (guía completa y seguridad):** [docs/guia-backend.md](docs/guia-backend.md)
- **Decisiones del equipo (con fecha):** [docs/bitacora.md](docs/bitacora.md)
- **Diseño:** [Figma](https://www.figma.com/design/MHJNZRX0wO86ZFuWTJiwzR/Familias-que-Suman-%E2%80%94-Prototipo)
- **Requerimientos:** https://docs.google.com/document/d/1E9YGMQR1R2KuULSeeNgVbwip-9pn6ljhmjwBu2FMb7o/edit?usp=sharing
- **Tareas:** Planner, plan "Reto TC2007B.402"
