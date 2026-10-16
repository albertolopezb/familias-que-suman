#!/usr/bin/env bash
# Prueba de contrato de la API (smoke test), como pide el anexo del reto en startdroid.
# Habla con la API de verdad por HTTP y revisa que cada respuesta sea la esperada.
#
#   cd backend && docker compose up -d --build      (la API levantada, con los datos de prueba)
#   bash smoke.sh                                   (contra http://127.0.0.1:8000)
#   bash smoke.sh https://<algo>.trycloudflare.com  (contra el túnel)
#
# Solo necesita bash y curl (Git Bash en Windows). Usa la cuenta admin@correo.com del seed.
#
# Ojo, los límites también se prueban aquí: el registro acepta 10 por hora por IP y el inicio de
# sesión 10 por minuto. Si lo corres muchas veces seguidas, algún paso puede dar 429: es el rate
# limit funcionando. Espera un minuto (o una hora para el registro) y vuelve a correrlo.

set -u
API="${1:-http://127.0.0.1:8000}"
API="${API%/}"
OK=0
FALLAS=0
N=0
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# pedir <método> <ruta> [token] [cuerpo json] [encabezado extra] → deja el código en $CODIGO y el cuerpo en $CUERPO
pedir() {
  local metodo="$1" ruta="$2" token="${3:-}" cuerpo="${4:-}" extra="${5:-}"
  local args=(-s -o "$TMP/cuerpo" -D "$TMP/encabezados" -w "%{http_code}" -X "$metodo" "$API$ruta")
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  [ -n "$cuerpo" ] && args+=(-H "Content-Type: application/json" -d "$cuerpo")
  [ -n "$extra" ] && args+=(-H "$extra")
  CODIGO="$(curl "${args[@]}")"
  CUERPO="$(cat "$TMP/cuerpo")"
  ENCABEZADOS="$(tr -d '\r' < "$TMP/encabezados" | tr 'A-Z' 'a-z')"
}

# revisar <descripción> <código esperado> [texto que debe venir en la respuesta]
revisar() {
  N=$((N + 1))
  local desc="$1" esperado="$2" texto="${3:-}"
  if [ "$CODIGO" = "$esperado" ] && { [ -z "$texto" ] || printf '%s' "$CUERPO$ENCABEZADOS" | grep -qi -- "$texto"; }; then
    OK=$((OK + 1)); printf '  ok   %2d  %s\n' "$N" "$desc"
  else
    FALLAS=$((FALLAS + 1)); printf '  FALLA %2d  %s  (esperado %s%s, llegó %s: %.160s)\n' "$N" "$desc" "$esperado" "${texto:+ con '$texto'}" "$CODIGO" "$CUERPO"
  fi
}

token_de() { printf '%s' "$CUERPO" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'; }
id_de() { printf '%s' "$CUERPO" | sed -n 's/^{"id":"\([^"]*\)".*/\1/p'; }

CORREO="smoke-$(date +%s)-$RANDOM@pruebas.mx"
CONTRASENA="una frase de prueba 2026"

echo "Prueba de contrato contra $API"
echo
echo "Público"
pedir GET /v1/salud;                                   revisar "La API está viva" 200 '"estado":"ok"'
pedir GET "/v1/actividades?cuando=proximas";           revisar "El catálogo es público" 200

echo "Identificación (JWT)"
pedir GET /v1/yo;                                      revisar "Sin token: 401 con WWW-Authenticate" 401 "www-authenticate: bearer"
pedir POST /v1/sesion/registro "" "{\"correo\":\"$CORREO\",\"contrasena\":\"$CONTRASENA\",\"nombre_familia\":\"Familia Smoke\",\"acepta_aviso\":true}"
revisar "Registro: crea la cuenta y responde el token" 201 '"token_type":"bearer"'
pedir POST /v1/sesion/registro "" "{\"correo\":\"$CORREO\",\"contrasena\":\"$CONTRASENA\",\"nombre_familia\":\"Familia Smoke\",\"acepta_aviso\":true}"
revisar "Registro con correo ocupado: 409" 409 "correo_ocupado"
pedir POST /v1/sesion/registro "" "{\"correo\":\"x$CORREO\",\"contrasena\":\"$CONTRASENA\",\"nombre_familia\":\"Familia Smoke\",\"acepta_aviso\":true,\"rol\":\"admin\"}"
revisar "Nadie se registra como admin (campo extra): 422" 422
pedir POST /v1/sesion/iniciar "" "{\"correo\":\"$CORREO\",\"contrasena\":\"no es esta\"}"
revisar "Contraseña equivocada: 401" 401 "credenciales_invalidas"
pedir POST /v1/sesion/iniciar "" "{\"correo\":\"nadie-$RANDOM@pruebas.mx\",\"contrasena\":\"no es esta\"}"
revisar "Correo inexistente: el mismo 401 (sin enumeración)" 401 "credenciales_invalidas"
pedir POST /v1/sesion/iniciar "" "{\"correo\":\"$CORREO\",\"contrasena\":\"$CONTRASENA\"}"
revisar "Iniciar sesión: 200 con el token" 200 '"access_token"'
FAMILIA="$(token_de)"
pedir GET /v1/yo "$FAMILIA";                           revisar "Con token: /v1/yo responde la cuenta y no es admin" 200 '"es_admin":false'
CABEZA="${FAMILIA%%.*}"; RESTO="${FAMILIA#*.}"; FIRMA="${RESTO#*.}"
ALTERADO="$CABEZA.eyJzdWIiOiJ5byIsInJvbCI6ImFkbWluIn0.$FIRMA"
pedir GET /v1/yo "$ALTERADO";                          revisar "Token alterado sin volver a firmar: 401" 401 "token_invalido"
pedir GET /v1/yo "esto.no.sirve";                      revisar "Token basura: 401" 401

echo "Autorización (ABAC)"
pedir GET /v1/admin/encuestas "$FAMILIA";              revisar "Una familia no entra a funciones de admin: 403" 403 "sin_permiso"
pedir POST /v1/sesion/iniciar "" '{"correo":"admin@correo.com","contrasena":"admin123"}'
revisar "Admin del seed inicia sesión" 200 '"access_token"'
ADMIN="$(token_de)"
pedir GET /v1/admin/encuestas "$ADMIN";                revisar "El admin sí entra a funciones de admin" 200
pedir POST /v1/yo/acompanantes "$ADMIN" '{"nombre":"Persona Smoke","edad":30}'
revisar "El admin registra a alguien de su familia" 201
AJENO="$(id_de)"
pedir DELETE "/v1/yo/acompanantes/$AJENO" "$FAMILIA";  revisar "BOLA: otra familia no borra lo ajeno (404, no 403)" 404 "no_encontrado"
pedir DELETE "/v1/yo/acompanantes/$AJENO" "$ADMIN";    revisar "Su dueña sí lo borra" 204

echo "Entradas"
pedir GET "/v1/actividades?ciudad=Monterrey%27%20OR%201%3D1--";  revisar "SQLi en un filtro: 422" 422
pedir GET "/v1/actividades/1%27%20OR%20%271%27%3D%271";          revisar "SQLi en un id: 422" 422

echo "HTTP"
pedir GET /v1/salud
revisar "Encabezado HSTS" 200 "strict-transport-security: max-age=31536000"
revisar "Encabezado nosniff" 200 "x-content-type-options: nosniff"
revisar "Encabezado CSP" 200 "content-security-policy: default-src 'none'"
pedir GET /v1/salud "" "" "Host: evil.example.com"
# Directo a la API responde ella (400). Por el túnel, Cloudflare lo rechaza antes de que llegue (403).
if [ "$CODIGO" = "403" ] && printf '%s' "$CUERPO" | grep -qi cloudflare; then CODIGO=400; CUERPO='host_invalido (lo rechazó Cloudflare)'; fi
revisar "Host ajeno: rechazado" 400 "host_invalido"

echo "Sesión"
pedir POST /v1/sesion/cerrar "$FAMILIA";               revisar "Cerrar sesión: 204" 204
pedir GET /v1/yo "$FAMILIA";                           revisar "El token cerrado ya no sirve: 401" 401 "sesion_cerrada"

echo "Fuerza bruta"
for _ in 1 2 3 4 5; do pedir POST /v1/sesion/iniciar "" "{\"correo\":\"$CORREO\",\"contrasena\":\"adivinando\"}"; done
pedir POST /v1/sesion/iniciar "" "{\"correo\":\"$CORREO\",\"contrasena\":\"$CONTRASENA\"}"
revisar "Tras 5 fallos, el correo se bloquea aunque la contraseña sea buena: 429" 429 "demasiados_intentos"

echo
echo "Resultado: $OK de $N pasaron, $FALLAS fallaron."
[ "$FALLAS" -eq 0 ]
