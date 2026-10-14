"""La conexión a PostgreSQL.

Se usa SQLAlchemy (un ORM). Todas las consultas se arman con objetos de Python, y SQLAlchemy las
manda a PostgreSQL como consultas parametrizadas (prepared statements): el texto que escribe una
persona viaja como DATO, nunca como parte del SQL. Eso es lo que cierra la puerta a la
inyección SQL (OWASP A05:2025 · CWE-89). En todo el proyecto no hay un solo f-string con SQL.
"""

from collections.abc import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, Session, sessionmaker

from .config import ajustes

engine = create_engine(
    ajustes.database_url,
    pool_size=10,
    max_overflow=10,
    pool_pre_ping=True,          # una conexión rota se detecta antes de usarse
    pool_recycle=1800,
    connect_args={
        # Ninguna consulta puede tardar más de 5 s ni esperar un candado más de 3 s:
        # una petición lenta no puede acaparar la base (OWASP API4 · consumo de recursos).
        "options": "-c statement_timeout=5000 -c lock_timeout=3000 -c idle_in_transaction_session_timeout=10000",
        "connect_timeout": 5,
        "application_name": "familias-api",
    },
)
SesionLocal = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)


class Base(DeclarativeBase):
    pass


def obtener_db() -> Generator[Session, None, None]:
    """Una sesión por petición: se abre al entrar y se cierra al salir, pase lo que pase."""
    db = SesionLocal()
    try:
        yield db
    finally:
        db.close()
