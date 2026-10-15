"""La configuración, en un solo lugar.

Todo lo que cambia entre tu máquina y un servidor (contraseñas, dominios, llaves) vive en
variables de entorno, nunca en el código (OWASP A02 · CWE-798 "Hard-coded credentials").
docker-compose.yml las pasa desde `backend/.env`.

En `produccion` la API se niega a arrancar si encuentra una contraseña de ejemplo o un secreto
de JWT corto: es preferible que no arranque a que arranque insegura ("fallar cerrado", OWASP A10).
"""

from functools import cached_property
from ipaddress import ip_address
from typing import Literal

from pydantic import field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Ajustes(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    entorno: Literal["desarrollo", "produccion"] = "desarrollo"

    # ── PostgreSQL ── La API entra con un usuario que solo puede leer y escribir filas; no puede
    # crear ni borrar tablas (ver db/migrations/afterMigrate.sql). Mínimo privilegio.
    database_url: str = "postgresql+psycopg://familias_api:cambia-esta-contrasena-api@db:5432/familias"

    # ── Redis ── contadores del rate limit y sesiones cerradas.
    redis_url: str = "redis://:cambia-esta-contrasena-redis@redis:6379/0"

    # ── JWT ── La API emite y verifica sus propios tokens (HS256, como en la guía de clase).
    # El SECRETO firma los tokens: quien lo tenga puede fabricar uno de administración, así que
    # vive solo aquí (variable de entorno), nunca en el código ni en la app. Mínimo 32 caracteres.
    jwt_secreto: str = "cambia-este-secreto-jwt-de-desarrollo-32+"
    jwt_emisor: str = "familias-que-suman-api"          # claim `iss`: quién lo firmó
    jwt_audiencia: str = "familias-que-suman-app"       # claim `aud`: para quién es
    # Cuánto dura una sesión. La app no pide la contraseña cada hora: un día es el punto medio
    # entre comodidad y riesgo (un token robado sirve como mucho 24 h, y "Cerrar sesión" lo
    # invalida al instante; ver identidad.SesionesCerradas).
    jwt_minutos: int = 1440

    # ── Fuerza bruta contra el inicio de sesión ── Tras N contraseñas equivocadas seguidas para
    # un mismo correo, ese correo queda bloqueado unos minutos (desde cualquier IP).
    login_max_fallos: int = 5
    login_bloqueo_minutos: int = 15

    # ── Almacén de fotos (S3: RustFS en Docker; Amazon S3 o R2 en la nube, sin cambiar código) ──
    s3_endpoint: str = "http://almacen:9000"
    s3_access_key: str = "familias-almacen"
    s3_secret_key: str = "cambia-esta-contrasena-almacen"
    s3_bucket: str = "familias"

    # Llave para seudonimizar IPs con HMAC-SHA256 en la bitácora: se pueden correlacionar
    # eventos de una misma IP sin guardar la IP (privacidad).
    hmac_secreto: str = "cambia-este-secreto-hmac"

    # ── Red ── Solo se cree el encabezado CF-Connecting-IP si la petición viene del túnel.
    ip_tunel: str = "172.31.250.10"
    hosts_permitidos: str = "localhost,127.0.0.1,10.0.2.2,api,*.trycloudflare.com"

    # ── Enlaces para compartir ── Vacío = se pregunta al túnel su dirección pública.
    enlaces_base: str = ""
    tunel_metricas_url: str = "http://tunel:2000"
    app_paquete: str = "mx.tec.familiasquesuman"
    app_esquema: str = "familiasquesuman"
    # Huellas SHA-256 del certificado con que se firma la app (para App Links). Separadas por coma.
    app_huellas_sha256: str = ""
    sitio_respaldo: str = "https://familiasquesuman.com/actividades"

    # ── Límites (OWASP API4 · consumo de recursos) ──
    limite_global: str = "120/minute"
    max_cuerpo_bytes: int = 1_000_000          # un JSON nunca pesa más de 1 MB
    max_foto_bytes: int = 6_000_000            # una foto de teléfono comprimida cabe de sobra
    max_foto_lado: int = 1600                  # se guarda reducida: menos espacio, sin metadatos

    # Métricas de Prometheus en un puerto aparte que el túnel no publica.
    metricas_puerto: int = 9100
    metricas_activas: bool = True

    @field_validator("jwt_minutos")
    @classmethod
    def _duracion_razonable(cls, v: int) -> int:
        if not 5 <= v <= 7 * 24 * 60:
            raise ValueError("JWT_MINUTOS debe estar entre 5 minutos y 7 días")
        return v

    @property
    def es_produccion(self) -> bool:
        return self.entorno == "produccion"

    @cached_property
    def ip_del_tunel(self):
        return ip_address(self.ip_tunel)

    @property
    def lista_hosts(self) -> list[str]:
        return [h.strip() for h in self.hosts_permitidos.split(",") if h.strip()]

    @property
    def lista_huellas(self) -> list[str]:
        return [h.strip().upper() for h in self.app_huellas_sha256.split(",") if h.strip()]

    def revisar_para_produccion(self) -> None:
        """Se llama al arrancar. En producción, una contraseña de ejemplo es un error fatal."""
        if not self.es_produccion:
            return
        problemas = []
        for nombre, valor in [
            ("DATABASE_URL", self.database_url),
            ("REDIS_URL", self.redis_url),
            ("S3_SECRET_KEY", self.s3_secret_key),
            ("HMAC_SECRETO", self.hmac_secreto),
            ("JWT_SECRETO", self.jwt_secreto),
        ]:
            if "cambia-" in valor:
                problemas.append(f"{nombre} todavía tiene el valor de ejemplo")
        if len(self.hmac_secreto) < 32:
            problemas.append("HMAC_SECRETO debe tener al menos 32 caracteres")
        if len(self.jwt_secreto) < 32:
            problemas.append("JWT_SECRETO debe tener al menos 32 caracteres")
        if self.jwt_secreto == self.hmac_secreto:
            problemas.append("JWT_SECRETO y HMAC_SECRETO deben ser distintos")
        if problemas:
            raise RuntimeError("La API no arranca en producción: " + "; ".join(problemas))


ajustes = Ajustes()
