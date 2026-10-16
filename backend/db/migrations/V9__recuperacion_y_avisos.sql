-- V9 · Recuperar la contraseña con un código por correo, y avisos que manda Familias que Suman.
--
--   1. `recuperaciones`: "¿Olvidaste tu contraseña?" manda un código de 6 dígitos por correo
--      (el anexo del reto: "con correo, hay un token de un solo uso"). Del código solo se guarda
--      su HMAC-SHA256: quien lea la tabla no puede usarlo. Dura 15 minutos, sirve una vez y se
--      quema tras 5 intentos equivocados (fuerza bruta).
--   2. `avisos`: las urgencias de una ciudad y las actividades nuevas (RF-22). La app los
--      consulta cada 15 minutos con WorkManager, como en la Práctica 8, sin Firebase.
--
-- Regla de siempre: este archivo no se edita una vez en main. Un cambio nuevo va en V10.

-- ─── 1. Recuperar la contraseña ──────────────────────────────────────────────
CREATE TABLE recuperaciones (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id  uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    huella     text NOT NULL CHECK (char_length(huella) = 64),   -- HMAC-SHA256 del código, nunca el código
    creada_en  timestamptz NOT NULL DEFAULT now(),
    expira_en  timestamptz NOT NULL,
    usada_en   timestamptz,                                       -- se usó, se reemplazó o se quemó
    intentos   integer NOT NULL DEFAULT 0 CHECK (intentos >= 0),
    CONSTRAINT recuperaciones_vigencia CHECK (expira_en > creada_en)
);
CREATE INDEX recuperaciones_cuenta_idx ON recuperaciones (cuenta_id, creada_en DESC);

-- ─── 2. Avisos ───────────────────────────────────────────────────────────────
CREATE TABLE avisos (
    id           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,   -- la app pide "los que van después del N"
    tipo         text NOT NULL CHECK (tipo IN ('urgencia', 'actividad_nueva')),
    ciudad       text REFERENCES ciudades (nombre),                 -- null = todas las ciudades
    titulo       text NOT NULL CHECK (char_length(titulo) BETWEEN 3 AND 80),
    cuerpo       text NOT NULL CHECK (char_length(cuerpo) BETWEEN 3 AND 300),
    actividad_id uuid REFERENCES actividades (id) ON DELETE SET NULL,
    creado_por   uuid,                                              -- sin FK: la constancia sobrevive a la cuenta
    creado_en    timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX avisos_ciudad_idx ON avisos (ciudad, id);
