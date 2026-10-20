#!/usr/bin/env bash
# Despliega el backend en una VM Ubuntu 24.04 por SSH (Azure for Students, Oracle Cloud o cualquier otra).
# Se corre desde tu computadora, en la raíz del repositorio:
#
#   bash backend/despliegue/desplegar.sh <IP pública> [usuario] [ruta de la llave SSH]
#
#   Azure:   bash backend/despliegue/desplegar.sh 20.98.33.13 azureuser
#   Oracle:  bash backend/despliegue/desplegar.sh <IP> ubuntu
#
# Qué hace (manual de instalación §6.3 y plan de implantación §7.4):
#   1. Instala Docker en la VM desde los paquetes de Ubuntu (sin scripts de terceros) y deja el
#      firewall de la VM en "negar por omisión": solo entra SSH (como el firewall stateful de la s24).
#   2. Sube backend/ tal como está en el último commit (git archive): se despliega lo versionado.
#   3. La primera vez crea backend/.env EN LA VM con secretos al azar y ENTORNO=produccion: la API se
#      niega a arrancar con un secreto de ejemplo. Los secretos nunca salen de la VM.
#   4. Levanta todo (base, migraciones, catálogo, almacén, Redis, API, monitoreo) y el túnel HTTPS.
#   5. La primera vez cambia las contraseñas de las cuentas de prueba del seed por unas al azar y las
#      deja en tu computadora, en D:/TEC/.llaves/cuentas-demo.txt (fuera del repo, no se imprimen).
#   6. Imprime la dirección https://…trycloudflare.com para compilar el APK de entrega.
#
# No hace falta abrir ningún puerto en la nube: el túnel sale hacia Cloudflare.
# Volver a correrlo actualiza el código y conserva .env, datos y contraseñas (redepliegue).

set -euo pipefail
IP="${1:?Uso: bash backend/despliegue/desplegar.sh <IP> [usuario] [llave]}"
USUARIO="${2:-ubuntu}"
LLAVE="${3:-D:/TEC/.llaves/oracle_familias}"
SSH=(ssh -i "$LLAVE" -o StrictHostKeyChecking=accept-new -o ServerAliveInterval=30 "$USUARIO@$IP")
cd "$(git rev-parse --show-toplevel)"

echo "1/6 Docker y firewall en la VM…"
"${SSH[@]}" 'command -v docker >/dev/null || { sudo apt-get update -qq && sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -qq docker.io docker-compose-v2 >/dev/null; }
  sudo systemctl enable --now docker >/dev/null
  sudo ufw default deny incoming >/dev/null && sudo ufw default allow outgoing >/dev/null && sudo ufw allow OpenSSH >/dev/null && sudo ufw --force enable >/dev/null
  sudo ufw status | head -1'

echo "2/6 Código (commit $(git rev-parse --short HEAD))…"
git -c core.autocrlf=false archive --format=tar HEAD backend | "${SSH[@]}" 'mkdir -p ~/familias && tar -x -C ~/familias'

echo "3/6 Secretos de producción (solo la primera vez)…"
# La huella del certificado con que se firma el APK: App Links (assetlinks.json) solo la acepta a ella.
HUELLA="${APP_HUELLA:-88:C7:BB:77:6B:BF:3C:49:3F:D3:38:CC:14:17:33:60:74:EB:65:52:BC:8E:88:45:EC:E6:AE:7C:E8:F2:87:0C}"
"${SSH[@]}" "HUELLA='$HUELLA' bash -s" <<'REMOTO'
set -euo pipefail
cd ~/familias/backend
if [ ! -f .env ]; then
  s() { python3 -c 'import secrets; print(secrets.token_urlsafe(36))'; }
  umask 077
  cat > .env <<EOF
ENTORNO=produccion
POSTGRES_PASSWORD=$(s)
DB_API_CONTRASENA=$(s)
JWT_SECRETO=$(s)
HMAC_SECRETO=$(s)
STORAGE_ROOT_USER=familias-almacen
STORAGE_ROOT_PASSWORD=$(s)
REDIS_CONTRASENA=$(s)
GRAFANA_CONTRASENA=$(s)
JWT_MINUTOS=15
REFRESH_DIAS=30
HOSTS_PERMITIDOS=*.trycloudflare.com,localhost,127.0.0.1,api
APP_HUELLAS_SHA256=$HUELLA
EOF
  echo "   .env creado con secretos nuevos (permisos 600)"
else
  echo "   .env ya existía: se conserva"
fi
REMOTO

echo "4/6 Levantando servicios y túnel (la primera vez tarda: construye la imagen de la API)…"
"${SSH[@]}" 'cd ~/familias/backend && sudo docker compose up -d --build && sudo docker compose --profile tunel up -d
  for _ in $(seq 1 60); do curl -fsS http://127.0.0.1:8000/v1/salud >/dev/null 2>&1 && exit 0; sleep 3; done
  echo "La API no respondió: sudo docker compose logs api"; exit 1'

echo "5/6 Cuentas de prueba con contraseñas nuevas (solo la primera vez)…"
DEMO="D:/TEC/.llaves/cuentas-demo.txt"
if [ ! -f "$DEMO" ]; then
  ( umask 077
    "${SSH[@]}" 'cd ~/familias/backend && sudo docker compose exec -T api python -' > "$DEMO" <<'PY'
import secrets
from app.herramientas import cambiar_contrasena
for correo in ("ana.rodriguez@correo.com", "admin@correo.com"):
    nueva = "demo-" + secrets.token_urlsafe(9)
    cambiar_contrasena(correo, nueva)
    print(correo, nueva)
PY
  )
  echo "   Guardadas en $DEMO (no se imprimen aquí)"
else
  echo "   Ya existían: se conservan ($DEMO)"
fi

echo "6/6 Dirección pública:"
for _ in $(seq 1 30); do
  URL=$("${SSH[@]}" 'cd ~/familias/backend && sudo docker compose logs tunel 2>&1 | grep -o "https://[a-z0-9-]*\.trycloudflare\.com" | tail -1' || true)
  [ -n "$URL" ] && break
  sleep 2
done
echo "   ${URL:-(todavía no aparece: docker compose logs tunel)}"
[ -n "${URL:-}" ] && curl -fsS "$URL/v1/salud" && echo
echo "Para la app: api.url.release=$URL en local.properties y ./gradlew :app:assembleRelease"
