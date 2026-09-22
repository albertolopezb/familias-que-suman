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
app/src/main/java/mx/tec/familiasquesuman/
├── domain/        modelos de la app
├── data/          repositorios: de dónde salen los datos
├── ui/screens/    las pantallas
├── ui/state/      ViewModels y estados de carga
├── ui/components/ piezas reutilizables
├── ui/navigation/ rutas y barra inferior
└── ui/theme/      colores y tipografías de la marca

backend/           servidor en Docker
```

## Etapas

- [ ] **1. Pantallas** — toda la app navegable con datos de prueba
- [ ] **2. Servidor** — base de datos y API; las pantallas leen datos reales
- [ ] **3. Cuenta y sesión** — registro, inicio de sesión e inscripciones
- [ ] **4. Notificaciones** — recordatorios y avisos de favoritos
- [ ] **5. Pruebas y entrega** — pruebas con familias reales y ajustes finales

## Documentación

- **Diseño:** [Figma](https://www.figma.com/design/MHJNZRX0wO86ZFuWTJiwzR/Familias-que-Suman-%E2%80%94-Prototipo)
- **Arquitectura:** (https://drive.google.com/file/d/1nym5BV6J6c_PGZgso-x5DDpPbicDLGjZ/view?usp=sharing)
- **Decisiones del equipo:** https://docs.google.com/document/d/1E9YGMQR1R2KuULSeeNgVbwip-9pn6ljhmjwBu2FMb7o/edit?usp=sharing
- **Tareas:** En Teams

---