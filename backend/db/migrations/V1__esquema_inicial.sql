-- Primera migración: lo mínimo para empezar. Solo 4 tablas.
--
-- Una migración es un archivo SQL que cambia la base de datos. Flyway corre los archivos en
-- orden (V1, V2, V3...) y apunta cuáles ya aplicó, así cada uno corre una sola vez.
-- Regla: este archivo NO se edita una vez que está en main. Un cambio nuevo va en V2.

-- Las cuentas de las familias y del admin.
CREATE TABLE cuentas (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    correo          text NOT NULL,
    contrasena_hash text NOT NULL,                 -- la contraseña va cifrada, nunca en claro
    rol             text NOT NULL DEFAULT 'familia' CHECK (rol IN ('familia', 'admin')),
    nombre_familia  text NOT NULL,
    ciudad          text NOT NULL DEFAULT 'Monterrey',
    creada_en       timestamptz NOT NULL DEFAULT now()
);
-- Dos cuentas no pueden tener el mismo correo, aunque cambien mayúsculas.
CREATE UNIQUE INDEX cuentas_correo_unico ON cuentas (lower(correo));

-- Las asociaciones que organizan actividades.
CREATE TABLE asociaciones (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      text NOT NULL,
    categoria   text NOT NULL,
    descripcion text NOT NULL DEFAULT '',
    ciudad      text NOT NULL DEFAULT 'Monterrey'
);

-- Las actividades. cupo_total = cuántos lugares hay.
CREATE TABLE actividades (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asociacion_id uuid NOT NULL REFERENCES asociaciones (id),
    titulo        text NOT NULL,
    descripcion   text NOT NULL DEFAULT '',
    inicia_en     timestamptz NOT NULL,
    direccion     text NOT NULL DEFAULT '',
    cupo_total    integer NOT NULL DEFAULT 0 CHECK (cupo_total >= 0)
);

-- Cuando una familia se inscribe a una actividad. "personas" es cuántos van en total.
-- cancelada_en vacío = la inscripción sigue vigente.
CREATE TABLE inscripciones (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id    uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    actividad_id uuid NOT NULL REFERENCES actividades (id),
    personas     integer NOT NULL CHECK (personas > 0),
    creada_en    timestamptz NOT NULL DEFAULT now(),
    cancelada_en timestamptz
);
-- Una familia no puede tener dos inscripciones vigentes en la misma actividad.
CREATE UNIQUE INDEX inscripciones_una_vigente ON inscripciones (cuenta_id, actividad_id)
    WHERE cancelada_en IS NULL;
