# 6. Manual de instalación

Tres formas de poner el sistema a funcionar, de la más simple a la completa:

| Quiero… | Sección |
|---|---|
| Solo usar la app contra el servidor que ya está en internet | §6.1 |
| Levantar todo en mi computadora (app en el emulador, API en Docker) | §6.2 |
| Publicar el servidor en internet (Azure for Students, sin tarjeta) | §6.3 |

Repositorio: https://github.com/albertolopezb/familias-que-suman (tag `v3.0-entrega`).

## 6.1 Instalar la app en un teléfono

Requisitos: teléfono Android 8.0 o superior con internet.

1. Descarga `familias-que-suman-v3.1.apk` (enlace en la sección 10 del documento).
2. Ábrelo en el teléfono. La primera vez, Android pide permitir **instalar apps de origen
   desconocido** para el navegador o el administrador de archivos: acéptalo solo para esta
   instalación.
3. Abre **Familias que Suman**, elige tu ciudad y listo.

**Por cable, desde una computadora** (depuración USB activada en *Opciones de desarrollador*):

```bash
adb install -r familias-que-suman-v3.1.apk
```

Comprobar que el APK es el original (firmado por el equipo):

```bash
apksigner verify --print-certs familias-que-suman-v3.1.apk
```

Debe decir `CN=Familias que Suman, OU=TC2007B` y la huella SHA-256 `88:c7:bb:77:…:87:0c`.

## 6.2 Todo en tu computadora

### 6.2.1 Requisitos

| Programa | Versión | Para qué |
|---|---|---|
| Git | cualquiera | Clonar |
| Docker Desktop | 4.x con Compose v2 | El backend completo |
| Android Studio | 2025.x o más reciente (trae el JDK 17+) | Compilar y correr la app |
| Espacio | ~6 GB | Imágenes de Docker y Gradle |

### 6.2.2 Backend

```bash
git clone https://github.com/albertolopezb/familias-que-suman.git
cd familias-que-suman/backend
docker compose up -d --build
```

La primera vez descarga las imágenes y construye la API (unos minutos). Después:

- Comprobar que está viva: abrir http://localhost:8000/v1/salud → `{"estado":"ok"}`.
- Documentación interactiva de la API (solo en desarrollo y desde tu máquina): http://localhost:8000/docs
- Correo de desarrollo (los códigos para recuperar la contraseña): http://localhost:8025
- Grafana: http://localhost:3000

No hace falta crear un `.env`: sin él se usan valores de desarrollo. Para cambiarlos, copia
`backend/.env.example` a `backend/.env`.

**Datos de prueba:** se cargan solos (asociaciones, actividades, campañas y proyectos de Monterrey y
Hermosillo). Cuentas de desarrollo: `ana.rodriguez@correo.com` (familia) y `admin@correo.com`
(administración). Sus contraseñas están en `backend/db/seed/dev.sql`, solo para desarrollo.

**Pruebas:**

```bash
docker compose --profile pruebas run --rm --build pruebas
```

```bash
bash smoke.sh
```

```bash
bash seguridad/zap/escanear.sh
```

- La primera son las 191 pruebas del servidor.
- La segunda es la prueba de contrato de 35 casos contra la API levantada.
- La tercera es OWASP ZAP sin y con sesión.

### 6.2.3 App en el emulador

1. Android Studio → **File → Open** → la carpeta `familias-que-suman` (la raíz, no `app/`). Espera
   el *Gradle sync*.
2. En `local.properties` (se crea solo; no se sube a Git) agrega:
   ```properties
   api.url=http://10.0.2.2:8000
   ```
   `10.0.2.2` es tu computadora vista desde el emulador.
3. ▶ **Run** con un emulador (API 26+). En Android 17, acepta el permiso de *red local* al abrir:
   sin él, la app no llega a tu computadora.

Pruebas de la app:

```bash
./gradlew :app:testDebugUnitTest
```

```bash
./gradlew :app:connectedDebugAndroidTest
```

La primera son las 66 pruebas locales. La segunda son las pruebas de interfaz y necesitan un
emulador o un teléfono conectado.

### 6.2.4 APK de entrega firmado

1. La llave de firma vive **fuera del repositorio**. Agrega a `local.properties`:
   ```properties
   api.url.release=https://<dirección del servidor>     # solo HTTPS
   firma.archivo=D\:/TEC/.llaves/familias-que-suman-release.jks
   firma.alias=familias-release
   firma.contrasena=<contraseña de la llave>
   ```
2. Compila:
   ```bash
   ./gradlew :app:assembleRelease
   ```
   Queda en `app/build/outputs/apk/release/app-release.apk`. Sin la llave, el APK sale sin firmar.
   Cada actualización debe ir firmada con la **misma** llave: respáldala.

### 6.2.5 Dar permisos de administración

Nadie se hace admin desde la app. Quien administra el servidor lo da:

```bash
docker compose exec api python -m app.herramientas permisos <correo> todos
```

Con `ninguno` se quitan. Para un permiso suelto: `moderar:contenido`, `registrar:asistencias`,
`administrar:catalogo` o `atender:privacidad`.

## 6.3 Servidor en internet: Azure for Students

Microsoft da **100 USD de crédito sin tarjeta** a estudiantes, verificando con el correo institucional.
La VM de la demostración cuesta unos 37 USD al mes (cómputo, disco e IP), corre el mismo
`docker compose` y no se duerme. Si se acaba el crédito, la suscripción se detiene: no hay cobros.

### 6.3.1 Crear la máquina (una vez)

1. Activar la suscripción en https://azure.microsoft.com/es-mx/free/students con el correo `@tec.mx`.
2. En https://portal.azure.com abrir **Cloud Shell** (ícono `>_`, modo Bash) y elegir la suscripción
   de estudiante. Si la cuenta también tiene la del Tec (*ITESM - LabVirtual*), esa no permite crear
   recursos:
   ```bash
   az account set --subscription "Azure for Students"
   ```
3. Ver en qué regiones deja crear recursos la suscripción. La de estudiante trae la política
   *Allowed resource deployment regions*; en octubre de 2026 eran `northcentralus`, `canadacentral`,
   `belgiumcentral`, `brazilsouth` y `norwayeast`, ninguna en México:
   ```bash
   az policy assignment list --disable-scope-strict-match --query "[].parameters.listOfAllowedLocations.value" -o json
   ```
4. Crear el grupo de recursos y la VM con Ubuntu 24.04, solo SSH y la llave **pública** (la privada
   se queda en tu computadora):
   ```bash
   az group create --name familias-que-suman-rg --location mexicocentral
   ```
   ```bash
   az vm create --resource-group familias-que-suman-rg --name familias-que-suman --location northcentralus --image Canonical:ubuntu-24_04-lts:server:latest --size Standard_B2als_v2 --admin-username azureuser --ssh-key-values "<llave pública ssh-ed25519>" --public-ip-sku Standard --os-disk-size-gb 64 --storage-sku StandardSSD_LRS --nsg-rule SSH
   ```
   Si responde *SkuNotAvailable* (sin capacidad), probar otro tamaño de 2 vCPU y 4 GB o más
   (`Standard_B2s_v2`, `Standard_B2as_v2`) u otra región permitida. La salida trae
   `publicIpAddress`.

El grupo de seguridad de red solo deja entrar SSH (22). No se abre ningún otro puerto: la API sale a
internet por el túnel.

### 6.3.2 Desplegar (y redesplegar)

Desde la raíz del repositorio, en tu computadora:

```bash
bash backend/despliegue/desplegar.sh <IP pública> azureuser <ruta a la llave privada>
```

El script:

1. Instala Docker desde los paquetes de Ubuntu y deja `ufw` en "negar por omisión", con solo SSH.
2. Sube `backend/` tal como está en el último commit.
3. La primera vez genera en la VM un `.env` con **secretos al azar** y `ENTORNO=produccion` (la API no
   arranca con un secreto de ejemplo).
4. Levanta todo con el túnel.
5. Cambia las contraseñas de las cuentas de prueba por unas al azar y las guarda **en tu
   computadora**, en `D:/TEC/.llaves/cuentas-demo.txt`.
6. Imprime la dirección `https://….trycloudflare.com`.

Volver a correrlo actualiza el código y conserva los datos y los secretos. El mismo script sirve para
Oracle Cloud Always Free (usuario `ubuntu`) o cualquier VM con Ubuntu 24.04.

### 6.3.3 Operación

Todo se hace por SSH (`ssh -i <llave> azureuser@<IP>`), en `~/familias/backend`:

| Para | Comando |
|---|---|
| Ver el estado | `sudo docker compose ps` |
| Ver la dirección pública del túnel | `sudo docker compose logs tunel \| grep trycloudflare` |
| Logs de la API | `sudo docker compose logs -f api` |
| Grafana y el correo de desarrollo desde tu computadora | `ssh -i <llave> -L 3000:127.0.0.1:3000 -L 8025:127.0.0.1:8025 azureuser@<IP>` y abrir http://localhost:3000 y http://localhost:8025 |
| Respaldo de la base | `sudo docker compose exec db pg_dump -U familias -d familias -Fc > respaldo-$(date +%F).dump` |

**Cuidar el crédito** (en Cloud Shell):

| Para | Comando |
|---|---|
| Apagar la VM (deja de cobrar el cómputo) | `az vm deallocate -g familias-que-suman-rg -n familias-que-suman` |
| Prenderla | `az vm start -g familias-que-suman-rg -n familias-que-suman` |
| Borrar todo al terminar el curso | `az group delete -n familias-que-suman-rg --yes` |

**La dirección de Quick Tunnel cambia si se reinicia el contenedor del túnel o la VM.** Si pasa, hay
que volver a compilar el APK con la nueva `api.url.release`. Con un dominio propio, el túnel con
nombre (`--profile tunel-nombrado`, guía §10.3) da una dirección fija.

## 6.4 Problemas comunes

| Síntoma | Qué hacer |
|---|---|
| `port is already allocated` | Otro programa usa el puerto: cambia `API_PORT` u otro puerto en `backend/.env` |
| La app en el emulador dice "sin conexión" | Android 17: acepta el permiso de red local, o `adb shell pm grant mx.tec.familiasquesuman android.permission.ACCESS_LOCAL_NETWORK` |
| El APK de entrega dice "sin conexión" | `api.url.release` apunta a un túnel viejo: revisa la dirección actual y vuelve a compilar |
| 429 `demasiados_intentos` al entrar | 5 contraseñas equivocadas: espera 15 minutos |
| Docker Desktop "unable to start" | Disco lleno: libera espacio |
| `SkuNotAvailable` o `RequestDisallowedByAzure` al crear la VM en Azure | Otro tamaño, o una región de las que permite la política de la suscripción (§6.3.1) |
| `AuthorizationFailed` al crear el grupo de recursos | Estás en la suscripción del Tec: `az account set --subscription "Azure for Students"` |

Más casos: guía del backend §14.5.
