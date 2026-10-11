-- Lo de cada familia: su foto, sus acompañantes, sus favoritos y sus avisos.

-- La foto de perfil vive en el almacenamiento de fotos; aquí solo su clave (nombre del archivo).
ALTER TABLE cuentas ADD COLUMN foto_clave text;

-- Hijos y demás familiares que la cuenta registra una vez y elige al inscribirse.
CREATE TABLE acompanantes (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id        uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    nombre           text NOT NULL,
    fecha_nacimiento date,
    creado_en        timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX acompanantes_cuenta_idx ON acompanantes (cuenta_id);

-- El corazón de favoritos. objeto_id apunta a la tabla que diga "tipo".
CREATE TABLE favoritos (
    cuenta_id uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    tipo      text NOT NULL CHECK (tipo IN ('asociacion', 'actividad', 'campana', 'proyecto', 'centro')),
    objeto_id uuid NOT NULL,
    creado_en timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (cuenta_id, tipo, objeto_id)
);

-- Los interruptores de Ajustes.
CREATE TABLE preferencias (
    cuenta_id                uuid PRIMARY KEY REFERENCES cuentas (id) ON DELETE CASCADE,
    recordatorio_actividades boolean NOT NULL DEFAULT true,
    avisos_favoritos         boolean NOT NULL DEFAULT true,
    urgencias_ciudad         boolean NOT NULL DEFAULT false
);
