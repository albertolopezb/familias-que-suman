-- Lo que las familias proponen y las solicitudes para borrar sus datos (HU-14).

CREATE TABLE sugerencias (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo               text NOT NULL CHECK (tipo IN ('actividad', 'campana', 'proyecto', 'centro')),
    nombre             text NOT NULL,
    descripcion        text NOT NULL,
    ciudad             text NOT NULL DEFAULT 'Monterrey',
    sugerida_por       text NOT NULL,
    contacto           text NOT NULL,
    cuenta_id          uuid REFERENCES cuentas (id) ON DELETE SET NULL,
    estado             text NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'aprobada', 'descartada')),
    creada_en          timestamptz NOT NULL DEFAULT now()
);

-- La solicitud se queda aunque la cuenta se borre: es la constancia. El folio que ve la
-- familia es 'ELIM-' más este número.
CREATE TABLE solicitudes_eliminacion (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    folio       bigint GENERATED ALWAYS AS IDENTITY UNIQUE,
    cuenta_id   uuid REFERENCES cuentas (id) ON DELETE SET NULL,
    correo      text NOT NULL,
    estado      text NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'atendida')),
    creada_en   timestamptz NOT NULL DEFAULT now()
);
-- Una sola solicitud pendiente por correo.
CREATE UNIQUE INDEX solicitudes_una_pendiente ON solicitudes_eliminacion (lower(correo))
    WHERE estado = 'pendiente';
