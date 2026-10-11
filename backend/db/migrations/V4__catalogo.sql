-- Las otras tres pestañas de la app: Proyectos, Directorio y Donar.

CREATE TABLE proyectos (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre        text NOT NULL,
    resumen       text NOT NULL DEFAULT '',
    descripcion   text NOT NULL DEFAULT '',
    beneficiarios text,
    ciudad        text NOT NULL DEFAULT 'Monterrey',
    activo        boolean NOT NULL DEFAULT true,
    telefono      text,
    whatsapp      text,
    logo_clave    text
);

-- "Elige tu forma de apoyar" de cada proyecto.
CREATE TABLE formas_apoyo (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    proyecto_id uuid NOT NULL REFERENCES proyectos (id) ON DELETE CASCADE,
    titulo      text NOT NULL,
    detalle     text NOT NULL DEFAULT ''
);

-- Directorio de visiteo (RF-03).
CREATE TABLE centros (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      text NOT NULL,
    tipo        text NOT NULL CHECK (tipo IN ('Asilos', 'Casas hogar', 'Comedores')),
    resumen     text NOT NULL DEFAULT '',
    necesidades text[] NOT NULL DEFAULT '{}',
    direccion   text NOT NULL DEFAULT '',
    verificado  boolean NOT NULL DEFAULT false,
    telefono    text,
    whatsapp    text,
    logo_clave  text
);

-- Campañas de donación en especie. La app no cobra: solo informa y conecta.
CREATE TABLE campanas (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asociacion_id uuid REFERENCES asociaciones (id),
    titulo        text NOT NULL,
    descripcion   text NOT NULL DEFAULT '',
    categoria     text NOT NULL DEFAULT '',
    cierra_en     date,
    urgente       boolean NOT NULL DEFAULT false,
    whatsapp      text,
    imagen_clave  text
);

-- Lo que se necesita juntar en una campaña con meta (por ejemplo, 300 cuentos).
CREATE TABLE articulos_meta (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    campana_id uuid NOT NULL REFERENCES campanas (id) ON DELETE CASCADE,
    nombre     text NOT NULL,
    meta       integer NOT NULL CHECK (meta > 0)
);

-- Lo que cada familia se compromete a aportar (RF-21).
CREATE TABLE aportaciones (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id   uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    articulo_id uuid NOT NULL REFERENCES articulos_meta (id) ON DELETE CASCADE,
    cantidad    integer NOT NULL CHECK (cantidad > 0),
    creada_en   timestamptz NOT NULL DEFAULT now()
);
