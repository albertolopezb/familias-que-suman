-- Callback de Flyway: corre DESPUÉS de cada `migrate`, aunque no haya migraciones nuevas.
-- No es una migración (no lleva versión), así que se puede editar.
--
-- MÍNIMO PRIVILEGIO para la API. Flyway entra como dueño de la base (crea y cambia tablas);
-- la API entra con otro usuario, `${api_usuario}`, que solo puede:
--   · leer y escribir filas de las tablas de la app,
--   · AGREGAR renglones a la bitácora de auditoría (no cambiarlos ni borrarlos),
--   · y nada más: no puede crear, alterar ni borrar tablas, ni leer el historial de Flyway.
-- Si alguien lograra inyectar SQL a través de la API (no debería: todo va parametrizado),
-- un DROP TABLE fallaría por falta de permisos. Defensa en profundidad.
--
-- La contraseña llega como placeholder de Flyway (FLYWAY_PLACEHOLDERS_API_CONTRASENA en
-- docker-compose.yml), nunca escrita aquí. Usa solo letras y números.

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${api_usuario}') THEN
        CREATE ROLE ${api_usuario} LOGIN;
    END IF;
END
$$;

ALTER ROLE ${api_usuario} WITH LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD '${api_contrasena}';
ALTER ROLE ${api_usuario} CONNECTION LIMIT 40;

GRANT CONNECT ON DATABASE ${flyway:database} TO ${api_usuario};
GRANT USAGE ON SCHEMA public TO ${api_usuario};
REVOKE CREATE ON SCHEMA public FROM PUBLIC;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO ${api_usuario};
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO ${api_usuario};

-- La bitácora: agregar y leer, nunca cambiar ni borrar.
REVOKE UPDATE, DELETE, TRUNCATE ON auditoria FROM ${api_usuario};
-- Las ciudades son datos de referencia: solo lectura.
REVOKE INSERT, UPDATE, DELETE, TRUNCATE ON ciudades FROM ${api_usuario};
-- El historial de migraciones no es asunto de la API.
REVOKE ALL ON flyway_schema_history FROM ${api_usuario};
