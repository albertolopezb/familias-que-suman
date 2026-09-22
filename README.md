# Familias que Suman — App Android

Kotlin + Jetpack Compose · Android 10 (API 29) o superior.

```
familias-que-suman/          ← se abre esta carpeta en Android Studio
├── app/                     ← el módulo Android
│   └── src/main/java/mx/tec/familiasquesuman/
│       ├── domain/          ← modelos, Kotlin puro
│       ├── data/            ← repositorios (hoy en memoria)
│       ├── ui/theme/        ← colores y tipografías de la marca
│       ├── ui/state/        ← ViewModels, UiState, Factory
│       ├── ui/screens/      ← una carpeta por tarea
│       ├── ui/components/   ← piezas reusables
│       ├── ui/navigation/   ← rutas y barra inferior
│       ├── FamiliasApplication.kt
│       └── MainActivity.kt
├── backend/                 ← docker-compose. Gradle lo ignora
├── docs/                    ← arquitectura y bitácora
├── gradle/  build.gradle.kts  settings.gradle.kts  gradlew
├── .gitignore
├── .env.example
└── README.md
```

## Para el equipo: cómo empezar

```bash
git clone https://github.com/USUARIO/familias-que-suman.git
```

Android Studio → **File → Open** → la carpeta `familias-que-suman`. Espera el sync de Gradle y ▶ Run.

Después, tu rama:

```bash
git switch -c feat/RF-04-actividades
```

---

## Cómo se armó el esqueleto (una sola persona, una sola vez)

### 1. Repositorio vacío en GitHub
**New repository** → `familias-que-suman` → **Private** → sin README ni .gitignore. Invita al equipo en **Settings → Collaborators**.

### 2. Proyecto en Android Studio
**File → New → New Project → Empty Activity**

| Campo | Valor |
|---|---|
| Name | `Familias que Suman` |
| Package name | `mx.tec.familiasquesuman` — revísalo, Android Studio lo autogenera distinto |
| Save location | una carpeta **nueva** llamada `familias-que-suman` |
| Minimum SDK | API 29 |
| Build configuration language | Kotlin DSL |

### 3. Dependencias
En `gradle/libs.versions.toml`, dentro de `[versions]`:

```toml
navigationCompose = "2.9.0"
```

Dentro de `[libraries]`:

```toml
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-compose-material-icons-core = { group = "androidx.compose.material", name = "material-icons-core" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeKtx" }
```

En `app/build.gradle.kts`, dentro de `dependencies { }`:

```kotlin
implementation(libs.androidx.navigation.compose)
implementation(libs.androidx.compose.material.icons.core)
implementation(libs.androidx.lifecycle.viewmodel.compose)
implementation(libs.androidx.lifecycle.runtime.compose)
```

**Sync Now.** Si Android Studio sugiere una versión más nueva de navegación, acéptala.

### 4. Fuentes
Descarga **Nunito** e **Inter** de fonts.google.com. Clic derecho en `app/src/main/res` → **New → Android Resource Directory** → tipo `font`. Copia estos archivos de la carpeta `static` y renómbralos exactamente así:

| Original | Renombrar a |
|---|---|
| `Nunito-Bold.ttf` | `nunito_bold.ttf` |
| `Inter_18pt-Regular.ttf` | `inter_regular.ttf` |
| `Inter_18pt-Medium.ttf` | `inter_medium.ttf` |
| `Inter_18pt-SemiBold.ttf` | `inter_semibold.ttf` |
| `Inter_18pt-Bold.ttf` | `inter_bold.ttf` |

### 5. Código
Todo lo de `codigo-para-app/` va a `app/src/main/java/mx/tec/familiasquesuman/`, respetando carpetas. Los tres archivos de `ui/theme/` y `MainActivity.kt` **reemplazan** a los que generó Android Studio.

> El `Theme.kt` generado activa *dynamic color*, que en Android 12+ pinta la app con los colores del fondo de pantalla y borra la marca. El nuestro lo quita.

### 6. Manifiesto
En `app/src/main/AndroidManifest.xml`, dentro de `<application`:

```xml
android:name=".FamiliasApplication"
android:allowBackup="false"
```

Sin `android:name`, el contenedor nunca se crea. `allowBackup="false"` evita que la sesión viaje en respaldos (Práctica 6).

### 7. Correr
▶ Run. Abre en **Inicio** y las cuatro pestañas cambian de pantalla.

### 8. Archivos de la raíz
Copia a la raíz del proyecto: `README.md`, `.env.example`, `backend/`, `docs/`, y el `.gitignore` **reemplazando** el que generó Android Studio.

### 9. Primer commit y push
El código base lo generó una IA, así que el commit lo declara, igual que en las prácticas:

```bash
git init
git add -A
git status          # NO deben aparecer local.properties ni ningún .env
git commit -m "Esqueleto: tema, dominio, repositorios en memoria y navegación" \
           -m "Co-Authored-By: Claude <noreply@anthropic.com>"
git branch -M main
git remote add origin https://github.com/USUARIO/familias-que-suman.git
git push -u origin main
```

### 10. Proteger `main`
**Settings → Branches → Add branch protection rule** (o **Settings → Rules → Rulesets**): requiere pull request y 1 aprobación. En repos privados gratuitos no se aplica; con el **GitHub Student Developer Pack** (GitHub Pro gratis) sí.

---

## Reglas del equipo

**Una rama por tarea**, con el RF en el nombre: `feat/RF-04-actividades`.

**Cada quien en su carpeta de `ui/screens/`.** El tema y la navegación los toca solo quien hizo el esqueleto.

**Las pantallas son tontas**, como en las prácticas: reciben datos y funciones, no un ViewModel ni un NavController. Así su `@Preview` funciona sola.

```kotlin
@Composable
fun DetalleActividadScreen(
    actividad: Actividad,
    onInscribirme: () -> Unit,
    onRegresar: () -> Unit
)
```

**El camino de los datos es siempre el mismo:** repositorio → ViewModel (con `UiState`) → NavHost → pantalla. Nadie importa `DatosDePrueba`; es `internal` a propósito.

**Nadie crea su propio modelo.** Si falta un campo, se agrega en `domain/Modelos.kt` en un PR aparte.

**Un commit por avance que funcione**, no uno gigante al final.

**Si usas IA, se declara** con `Co-Authored-By` en el commit, igual que en las prácticas.

## Lo que nunca se sube
`local.properties`, `.env`, `google-services.json`, llaves `.jks`. Ya están en el `.gitignore`.
