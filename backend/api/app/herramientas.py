"""Herramientas de operación que se corren DENTRO del contenedor de la API (nunca por HTTP).

    docker compose exec api python -m app.herramientas vincular ana.rodriguez@correo.com "auth0|65f0c…"

`vincular`: une una cuenta que ya existía en la base (por ejemplo, la familia de prueba del seed)
con su usuario de Auth0. La API solo vincula sola cuando Auth0 asegura que el correo está
VERIFICADO (si no, cualquiera se registraría con el correo de otra familia y se quedaría con sus
datos). Los correos de prueba (`@correo.com`) no reciben correos, así que no se pueden verificar:
para eso existe esto. Como hay que tener acceso al servidor para correrlo, no abre ninguna puerta.

El `sub` se copia del panel de Auth0: User Management → Users → (la persona) → user_id.
"""

import re
import sys

from sqlalchemy import func, select

from .bitacora import auditar
from .db import SesionLocal
from .modelos import Cuenta

SUB_VALIDO = re.compile(r"[A-Za-z0-9_-]{1,40}\|[A-Za-z0-9_.-]{1,200}")


def vincular(correo: str, sub: str) -> int:
    if not SUB_VALIDO.fullmatch(sub):
        print("El sub no se ve como uno de Auth0 (ejemplo: auth0|65f0c1…).")
        return 2
    with SesionLocal() as db:
        if db.scalar(select(Cuenta).where(Cuenta.auth0_sub == sub)) is not None:
            print("Ese usuario de Auth0 ya tiene una cuenta en la base. No se cambió nada.")
            return 1
        cuenta = db.scalar(select(Cuenta).where(func.lower(Cuenta.correo) == correo.strip().lower()))
        if cuenta is None:
            print("No hay ninguna cuenta con ese correo.")
            return 1
        if cuenta.auth0_sub is not None:
            print("Esa cuenta ya está vinculada a otro usuario de Auth0. No se cambió nada.")
            return 1
        cuenta.auth0_sub = sub
        cuenta.correo_verificado = True
        auditar(db, "cuenta.vincular_manual", "cuenta", cuenta.id, None, {"sub": sub})
        db.commit()
        print(f"Listo: {cuenta.correo} ({cuenta.nombre_familia}) quedó vinculada a {sub}.")
        return 0


if __name__ == "__main__":
    if len(sys.argv) == 4 and sys.argv[1] == "vincular":
        sys.exit(vincular(sys.argv[2], sys.argv[3]))
    print(__doc__)
    sys.exit(2)
