# Familias que Suman — App Android

App para que las familias de Monterrey encuentren actividades de voluntariado, se inscriban con sus hijos y apoyen campañas de donación de Familias que Suman.

**Kotlin · Jetpack Compose · Android 10 (API 29) o superior**

## Correr el proyecto

```bash
git clone https://github.com/albertolopezb/familias-que-suman.git
```

Android Studio → **File → Open** → carpeta `familias-que-suman` → esperar el sync de Gradle → ▶ **Run**.


## Estructura actual

```
familias-que-suman/          ← se abre esta carpeta en Android Studio
├── app/                     ← el módulo Android
│   └── src/main/java/mx/tec/familiasquesuman/
│       ├── domain/          ← modelos, Kotlin puro
│       ├── data/            ← repositorios (API) y sesión (JWT de la API)
│       ├── ui/theme/        ← colores y tipografías de la marca
│       ├── ui/state/        ← ViewModels, UiState, Factory
│       ├── ui/screens/      ← una carpeta por tarea
│       ├── ui/components/   ← piezas reusables
│       ├── ui/navigation/   ← rutas y barra inferior
│       ├── FamiliasApplication.kt
│       └── MainActivity.kt
├── backend/                 ← API, base, monitoreo y seguridad (Docker). Gradle lo ignora
├── gradle/  build.gradle.kts  settings.gradle.kts  gradlew
├── .gitignore
├── .env.example
└── README.md
```

## Etapas

- [ ] **1. Pantallas** — toda la app navegable con datos de prueba
- [x] **2. Servidor** — base de datos y API; las pantallas leen datos reales
- [x] **3. Cuenta y sesión** — registro, inicio de sesión (JWT propio de la API) e inscripciones
- [ ] **4. Notificaciones** — recordatorios y avisos de favoritos
- [ ] **5. Pruebas y entrega** — pruebas con familias reales y ajustes finales

## Forma de trabajo

Una rama por tarea, con el RF en el nombre: feat/RF-04-actividades.

## Documentación

- **Diseño:** [Figma](https://www.figma.com/design/MHJNZRX0wO86ZFuWTJiwzR/Familias-que-Suman-%E2%80%94-Prototipo)
- **Arquitectura:** (https://drive.google.com/file/d/1nym5BV6J6c_PGZgso-x5DDpPbicDLGjZ/view?usp=sharing)
- **Backend (guía completa y seguridad):** [docs/guia-backend.md](docs/guia-backend.md)
- **Decisiones del equipo:** https://docs.google.com/document/d/1E9YGMQR1R2KuULSeeNgVbwip-9pn6ljhmjwBu2FMb7o/edit?usp=sharing
- **Tareas:** En Teams

---
