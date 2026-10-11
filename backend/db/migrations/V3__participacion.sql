-- Lo que pasa después de inscribirse: asistencias, testimonios, encuestas e invitaciones.

-- El historial (RF-11) se arma con lo que registra la asociación, no con lo que diga la familia.
CREATE TABLE asistencias (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id     uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    actividad_id  uuid NOT NULL REFERENCES actividades (id),
    registrada_en timestamptz NOT NULL DEFAULT now(),
    UNIQUE (cuenta_id, actividad_id)
);

-- RF-12: un testimonio por asistencia, con foto. Se publica al enviarse; lo borra su autor o el admin.
CREATE TABLE testimonios (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asistencia_id uuid NOT NULL UNIQUE REFERENCES asistencias (id) ON DELETE CASCADE,
    experiencia   text NOT NULL CHECK (char_length(btrim(experiencia)) BETWEEN 20 AND 500),
    foto_clave    text NOT NULL,
    creado_en     timestamptz NOT NULL DEFAULT now()
);

-- RF-13: las encuestas de antes y después de una actividad. Cada respuesta guarda la pregunta
-- y la opción tal como se leyeron: [{"numero":1,"pregunta":"...","respuesta":"..."}].
CREATE TABLE respuestas_encuesta (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id    uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    actividad_id uuid NOT NULL REFERENCES actividades (id),
    momento      text NOT NULL CHECK (momento IN ('antes', 'despues')),
    respuestas   jsonb NOT NULL,
    creada_en    timestamptz NOT NULL DEFAULT now(),
    UNIQUE (cuenta_id, actividad_id, momento)
);

-- Cada vez que una familia comparte la invitación a una actividad (insignia "Familia que invita").
CREATE TABLE invitaciones (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id    uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    actividad_id uuid NOT NULL REFERENCES actividades (id),
    creada_en    timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX invitaciones_cuenta_idx ON invitaciones (cuenta_id);
