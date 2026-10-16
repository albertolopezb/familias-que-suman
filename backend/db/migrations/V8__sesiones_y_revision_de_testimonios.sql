-- V8 · Sesiones que se renuevan (refresh tokens) y revisión de testimonios antes de publicarse.
--
--   1. `sesiones`: cada inicio de sesión entrega un access token corto (JWT de 15 min) y un
--      refresh token largo (30 días) para pedir otro sin volver a escribir la contraseña. Del
--      refresh token solo se guarda su HUELLA (SHA-256): quien robe la tabla no obtiene tokens.
--      Cada refresh token sirve UNA vez (rotación); si alguien reusa uno viejo, se cierra toda la
--      sesión ("familia" de tokens), porque significa que alguien más lo tenía.
--   2. Testimonios: nada se publica sin que Familias que Suman lo apruebe (RF-12, HU-11).
--
-- Regla de siempre: este archivo no se edita una vez en main. Un cambio nuevo va en V9.

-- ─── 1. Sesiones ─────────────────────────────────────────────────────────────
CREATE TABLE sesiones (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id   uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    familia     uuid NOT NULL,                      -- todas las renovaciones de un mismo inicio de sesión
    huella      text NOT NULL UNIQUE CHECK (char_length(huella) = 64),   -- SHA-256 en hex, nunca el token
    creada_en   timestamptz NOT NULL DEFAULT now(),
    expira_en   timestamptz NOT NULL,
    usada_en    timestamptz,                        -- se cambió por uno nuevo (rotación)
    revocada_en timestamptz,                        -- se cerró la sesión o se detectó reuso
    CONSTRAINT sesiones_vigencia CHECK (expira_en > creada_en)
);
CREATE INDEX sesiones_cuenta_idx ON sesiones (cuenta_id);
CREATE INDEX sesiones_familia_idx ON sesiones (familia);

-- ─── 2. Revisión de testimonios ──────────────────────────────────────────────
ALTER TABLE testimonios
    ADD COLUMN estado       text NOT NULL DEFAULT 'pendiente'
                            CHECK (estado IN ('pendiente', 'aprobado', 'rechazado')),
    ADD COLUMN revisado_en  timestamptz,
    ADD COLUMN revisado_por uuid;                   -- sin FK a propósito: la constancia sobrevive a la cuenta
CREATE INDEX testimonios_estado_idx ON testimonios (estado, creado_en DESC);

-- Lo que ya estaba publicado antes de esta versión se queda publicado.
UPDATE testimonios SET estado = 'aprobado', revisado_en = now();
