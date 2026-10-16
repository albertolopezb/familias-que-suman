#!/usr/bin/env bash
# El escaneo de OWASP ZAP en sus dos pasadas: sin sesión y con sesión (plan-zap.yaml).
#
#   cd backend && docker compose up -d --build                  (la API levantada)
#   bash seguridad/zap/escanear.sh                               (ZAP en Docker, perfil `zap`)
#   ZAP_JAR=<ruta>/zap-2.17.0.jar bash seguridad/zap/escanear.sh (ZAP instalado, sin bajar la imagen)
#
# Qué hace:
#   1. Sube LIMITE_GLOBAL un rato: si no, ZAP solo vería 429 y no probaría la lógica.
#   2. Crea una cuenta de escaneo SIN datos reales (POST /v1/sesion/registro) y pide su token
#      (POST /v1/sesion/iniciar). Es una familia normal: sin permisos de administración.
#   3. Corre el plan con ese token y deja los reportes en seguridad/zap/. El job `replacer` de ZAP
#      NO expande variables de entorno en sus reglas (manda "Bearer ${TOKEN}" tal cual: se
#      comprobó con un servidor de eco), así que se escribe una copia temporal del plan con el
#      token ya puesto (.plan-con-token.yaml, en .gitignore) y se borra al terminar.
#   4. Pase lo que pase: borra la cuenta (y con ella todo lo que ZAP le haya creado) y regresa
#      el límite normal. Con la cuenta borrada, el token que quede en algún log ya no sirve.

set -euo pipefail
cd "$(dirname "$0")/../.."   # backend/
API="${API:-http://127.0.0.1:8000}"
# Sin `date +%s` a propósito: un número de 10 cifras en /v1/yo (el correo) ZAP lo reporta como
# "Timestamp Disclosure - Unix" (10096). Pasó en la primera corrida: era el propio correo.
CORREO="zap-$(date +%Y%m%d-%H%M%S)-$RANDOM@pruebas.mx"
CONTRASENA="escaneo de zap $RANDOM-$RANDOM"

PLAN=seguridad/zap/.plan-con-token.yaml

limpiar() {
  rm -f "$PLAN"
  echo "Borrando la cuenta de escaneo y regresando el límite normal…"
  # El correo lo arma este script (solo letras, números y guiones): va tal cual en el SQL.
  docker compose exec -T db sh -c "psql -q -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM cuentas WHERE correo = '$CORREO'\"" || true
  docker compose up -d api >/dev/null
}

echo "Subiendo LIMITE_GLOBAL para el escaneo…"
LIMITE_GLOBAL=100000/minute docker compose up -d --wait api >/dev/null
trap limpiar EXIT

cuerpo=$(curl -fsS -X POST "$API/v1/sesion/registro" -H "Content-Type: application/json" \
  -d "{\"correo\":\"$CORREO\",\"contrasena\":\"$CONTRASENA\",\"nombre_familia\":\"Familia Escaneo ZAP\",\"acepta_aviso\":true}") \
  || { echo "No se pudo crear la cuenta de escaneo (¿429 del registro? espera una hora)."; exit 1; }
cuerpo=$(curl -fsS -X POST "$API/v1/sesion/iniciar" -H "Content-Type: application/json" \
  -d "{\"correo\":\"$CORREO\",\"contrasena\":\"$CONTRASENA\"}")
TOKEN=$(printf '%s' "$cuerpo" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')
[ -n "$TOKEN" ] || { echo "El inicio de sesión no regresó token."; exit 1; }
echo "Cuenta de escaneo: $CORREO (token listo)"
( umask 077; sed "s|\${TOKEN}|$TOKEN|" seguridad/zap/plan-zap.yaml > "$PLAN" )

codigo=0
if [ -n "${ZAP_JAR:-}" ]; then
  REPORTES="$(pwd)/seguridad/zap"
  command -v cygpath >/dev/null && REPORTES="$(cygpath -m "$REPORTES")"
  OBJETIVO="$API" REPORTES="$REPORTES" "${JAVA:-java}" -Xmx2g -jar "$ZAP_JAR" -cmd -silent \
    ${ZAP_HOME:+-dir "$ZAP_HOME"} -autorun "$REPORTES/.plan-con-token.yaml" || codigo=$?
else
  docker compose --profile zap run --rm zap zap.sh -cmd -autorun /zap/wrk/.plan-con-token.yaml || codigo=$?
fi
echo "ZAP terminó con código $codigo (0 = sin altas ni medias, 1 = alguna alta, 2 = alguna media)."
exit "$codigo"
