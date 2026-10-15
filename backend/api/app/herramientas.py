"""Herramientas de operación que se corren DENTRO del contenedor de la API (nunca por HTTP).

    docker compose exec api python -m app.herramientas permisos ana.rodriguez@correo.com todos
    docker compose exec api python -m app.herramientas permisos ana.rodriguez@correo.com moderar:contenido
    docker compose exec api python -m app.herramientas permisos ana.rodriguez@correo.com ninguno
    docker compose exec -it api python -m app.herramientas contrasena ana.rodriguez@correo.com

`permisos`: le da (o le quita) los permisos de administración a una cuenta. Nadie puede hacerse
admin desde la app: el registro siempre crea familias, y el cuerpo de las peticiones no acepta
campos como "permisos" (asignación masiva, OWASP API3). Hay que tener acceso al servidor.

`contrasena`: le pone una contraseña nueva a una cuenta (cuando la familia pidió ayuda desde
"¿Olvidaste tu contraseña?"). Se escribe sin que se vea en pantalla y NO va en la línea de
comandos: así no se queda en el historial de la terminal.

Las dos invalidan los tokens que la cuenta ya tenía (`tokens_validos_desde`): los permisos viajan
dentro del JWT, así que un cambio de permisos solo cuenta de verdad si los tokens viejos dejan de servir.
"""

import getpass
import sys
from datetime import UTC, datetime

from sqlalchemy import func, select

from . import esquemas
from .bitacora import auditar
from .db import SesionLocal
from .identidad import PERMISOS_ADMIN, hash_contrasena, rol_de
from .modelos import Cuenta


def _cuenta(db, correo: str) -> Cuenta | None:
    cuenta = db.scalar(select(Cuenta).where(func.lower(Cuenta.correo) == correo.strip().lower()))
    if cuenta is None:
        print("No hay ninguna cuenta con ese correo.")
    return cuenta


def dar_permisos(correo: str, pedidos: list[str]) -> int:
    if pedidos == ["todos"]:
        nuevos = set(PERMISOS_ADMIN)
    elif pedidos == ["ninguno"]:
        nuevos = set()
    else:
        desconocidos = [p for p in pedidos if p not in PERMISOS_ADMIN]
        if desconocidos or not pedidos:
            print("Permisos válidos: todos, ninguno, o uno o más de:", ", ".join(sorted(PERMISOS_ADMIN)))
            return 2
        nuevos = set(pedidos)
    with SesionLocal() as db:
        cuenta = _cuenta(db, correo)
        if cuenta is None:
            return 1
        antes = sorted(cuenta.permisos or [])
        cuenta.permisos = sorted(nuevos)
        cuenta.rol = rol_de(nuevos)
        cuenta.tokens_validos_desde = datetime.now(UTC)
        auditar(db, "cuenta.cambiar_permisos", "cuenta", cuenta.id, None, {"antes": antes, "despues": sorted(nuevos)})
        db.commit()
        print(f"Listo: {cuenta.correo} ahora tiene {sorted(nuevos) or 'ningún permiso'} (rol {cuenta.rol}). "
              "Tiene que volver a iniciar sesión.")
        return 0


def cambiar_contrasena(correo: str, nueva: str) -> int:
    try:
        esquemas.RegistroEntrada(correo=correo, contrasena=nueva, nombre_familia="Validación", acepta_aviso=True)
    except ValueError as error:
        print("Esa contraseña no sirve:", error.errors()[0]["msg"] if hasattr(error, "errors") else error)
        return 2
    with SesionLocal() as db:
        cuenta = _cuenta(db, correo)
        if cuenta is None:
            return 1
        cuenta.contrasena_hash = hash_contrasena(nueva)
        cuenta.tokens_validos_desde = datetime.now(UTC)
        auditar(db, "cuenta.cambiar_contrasena", "cuenta", cuenta.id, None)
        db.commit()
        print(f"Listo: {cuenta.correo} ya puede entrar con la contraseña nueva. Sus sesiones abiertas se cerraron.")
        return 0


if __name__ == "__main__":
    if len(sys.argv) >= 4 and sys.argv[1] == "permisos":
        sys.exit(dar_permisos(sys.argv[2], sys.argv[3:]))
    if len(sys.argv) == 3 and sys.argv[1] == "contrasena":
        primera = getpass.getpass("Contraseña nueva: ")
        if primera != getpass.getpass("Repítela: "):
            print("No coinciden. No se cambió nada.")
            sys.exit(2)
        sys.exit(cambiar_contrasena(sys.argv[2], primera))
    print(__doc__)
    sys.exit(2)
