-- V6 · Lo que faltaba para que la API sirva todo lo que muestra la app.
--
--   1. Las ciudades donde opera Familias que Suman (con su zona horaria).
--   2. Las cuentas pasan a Auth0: las contraseñas YA NO viven aquí.
--   3. Los textos completos de actividades, campañas, proyectos y centros que trae el sitio.
--   4. Quién va a cada inscripción, y el consentimiento cuando van menores.
--   5. La bitácora de auditoría.
--
-- Regla de siempre: este archivo no se edita una vez en main. Un cambio nuevo va en V7.

-- ─── 1. Ciudades ─────────────────────────────────────────────────────────────
CREATE TABLE ciudades (
    nombre       text PRIMARY KEY,
    estado       text NOT NULL,                       -- 'N.L.', 'Son.'
    zona_horaria text NOT NULL,                       -- IANA: las horas se muestran en la de la ciudad
    latitud      double precision NOT NULL CHECK (latitud BETWEEN -90 AND 90),
    longitud     double precision NOT NULL CHECK (longitud BETWEEN -180 AND 180)
);
INSERT INTO ciudades (nombre, estado, zona_horaria, latitud, longitud) VALUES
    ('Monterrey',  'N.L.', 'America/Monterrey',  25.6694, -100.3098),
    ('Hermosillo', 'Son.', 'America/Hermosillo', 29.0729, -110.9559);

-- ─── 2. Cuentas: la identidad la lleva Auth0 ────────────────────────────────
-- Auth0 guarda la contraseña (con hash bcrypt) y la protege contra fuerza bruta. Aquí ya no
-- queda ni el hash: un dato que no se guarda es un dato que no se puede robar.
ALTER TABLE cuentas DROP COLUMN contrasena_hash;
ALTER TABLE cuentas
    ADD COLUMN auth0_sub          text,                -- el "sub" del token: 'auth0|65f…'
    ADD COLUMN correo_verificado  boolean NOT NULL DEFAULT false,
    ADD COLUMN titular_nombre     text,
    ADD COLUMN titular_nacimiento date,
    ADD COLUMN titular_sexo       text CHECK (titular_sexo IN ('MASCULINO', 'FEMENINO', 'OTRO', 'NO_ESPECIFICA')),
    ADD COLUMN aviso_aceptado_en  timestamptz,         -- cuándo aceptó el aviso de privacidad
    ADD COLUMN ultimo_acceso_en   timestamptz,
    ADD CONSTRAINT cuentas_ciudad_fk FOREIGN KEY (ciudad) REFERENCES ciudades (nombre);
CREATE UNIQUE INDEX cuentas_auth0_sub_unico ON cuentas (auth0_sub);

-- ─── 3. Asociaciones ─────────────────────────────────────────────────────────
ALTER TABLE asociaciones
    ADD COLUMN direccion     text NOT NULL DEFAULT '',
    ADD COLUMN telefono      text NOT NULL DEFAULT '',
    ADD COLUMN whatsapp      text NOT NULL DEFAULT '',
    ADD COLUMN correo        text NOT NULL DEFAULT '',
    -- false = organiza actividades pero no sale en el directorio de Explorar.
    ADD COLUMN en_directorio boolean NOT NULL DEFAULT true,
    ADD CONSTRAINT asociaciones_ciudad_fk FOREIGN KEY (ciudad) REFERENCES ciudades (nombre);

-- ─── 4. Actividades ──────────────────────────────────────────────────────────
ALTER TABLE actividades
    ADD COLUMN termina_en          timestamptz,
    ADD COLUMN ciudad              text NOT NULL DEFAULT 'Monterrey' REFERENCES ciudades (nombre),
    ADD COLUMN municipio           text NOT NULL DEFAULT '',
    ADD COLUMN edad_minima         integer CHECK (edad_minima BETWEEN 0 AND 99),
    ADD COLUMN tema                text NOT NULL DEFAULT 'GENERAL'
                                   CHECK (tema IN ('SALUD', 'CELEBRACION', 'MEDIO_AMBIENTE', 'GENERAL')),
    ADD COLUMN aportacion_tipo     text NOT NULL DEFAULT 'ninguna'
                                   CHECK (aportacion_tipo IN ('ninguna', 'especie', 'monetaria')),
    ADD COLUMN aportacion_monto    text NOT NULL DEFAULT '',
    ADD COLUMN aportacion_detalle  text NOT NULL DEFAULT '',
    ADD COLUMN punto_encuentro     text NOT NULL DEFAULT '',
    ADD COLUMN acerca_del_proyecto text NOT NULL DEFAULT '',
    ADD COLUMN que_haremos         text NOT NULL DEFAULT '',
    ADD COLUMN que_incluye         text NOT NULL DEFAULT '',
    ADD COLUMN que_llevar          text NOT NULL DEFAULT '',
    ADD COLUMN recomendaciones     text NOT NULL DEFAULT '',
    ADD COLUMN imagen              text,                -- nombre de la imagen incluida en la app
    -- Lugares que se apartaron por fuera de la app (por teléfono, en persona).
    ADD COLUMN ocupados_fuera      integer NOT NULL DEFAULT 0 CHECK (ocupados_fuera >= 0),
    ADD COLUMN latitud             double precision CHECK (latitud BETWEEN -90 AND 90),
    ADD COLUMN longitud            double precision CHECK (longitud BETWEEN -180 AND 180),
    ADD COLUMN creada_en           timestamptz NOT NULL DEFAULT now(),
    ADD CONSTRAINT actividades_horario_valido CHECK (termina_en IS NULL OR termina_en > inicia_en);
CREATE INDEX actividades_ciudad_fecha_idx ON actividades (ciudad, inicia_en);

-- ─── 5. Inscripciones: quién va ──────────────────────────────────────────────
ALTER TABLE inscripciones
    ADD COLUMN actualizada_en            timestamptz NOT NULL DEFAULT now(),
    -- Si van menores, la persona titular acepta ser responsable. Se guarda cuándo.
    ADD COLUMN consentimiento_menores_en timestamptz;

-- La foto de quién va EN ESA inscripción: si mañana cambian los datos de un acompañante,
-- lo que se registró para la actividad no cambia.
CREATE TABLE inscripcion_personas (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    inscripcion_id   uuid NOT NULL REFERENCES inscripciones (id) ON DELETE CASCADE,
    nombre           text NOT NULL CHECK (char_length(btrim(nombre)) BETWEEN 1 AND 80),
    fecha_nacimiento date,
    edad             integer CHECK (edad BETWEEN 0 AND 120),
    sexo             text CHECK (sexo IN ('MASCULINO', 'FEMENINO', 'OTRO', 'NO_ESPECIFICA')),
    orden            integer NOT NULL DEFAULT 0
);
CREATE INDEX inscripcion_personas_inscripcion_idx ON inscripcion_personas (inscripcion_id);
CREATE INDEX inscripciones_actividad_vigentes_idx ON inscripciones (actividad_id) WHERE cancelada_en IS NULL;

-- ─── 6. Acompañantes ─────────────────────────────────────────────────────────
ALTER TABLE acompanantes
    ADD COLUMN edad integer CHECK (edad BETWEEN 0 AND 120),
    ADD COLUMN sexo text CHECK (sexo IN ('MASCULINO', 'FEMENINO', 'OTRO', 'NO_ESPECIFICA'));

-- ─── 7. Participación ────────────────────────────────────────────────────────
-- La API exige la foto al publicar; los testimonios de ejemplo del seed no traen.
ALTER TABLE testimonios ALTER COLUMN foto_clave DROP NOT NULL;
-- La encuesta de después se contesta sobre una participación (asistencia) concreta.
ALTER TABLE respuestas_encuesta
    ADD COLUMN asistencia_id uuid REFERENCES asistencias (id) ON DELETE CASCADE;

-- ─── 8. Proyectos ────────────────────────────────────────────────────────────
ALTER TABLE proyectos
    ADD COLUMN vigencia    text,
    ADD COLUMN acerca_de   text NOT NULL DEFAULT '',
    ADD COLUMN como_ayudar text NOT NULL DEFAULT '',
    ADD COLUMN instagram   text,
    ADD COLUMN latitud     double precision CHECK (latitud BETWEEN -90 AND 90),
    ADD COLUMN longitud    double precision CHECK (longitud BETWEEN -180 AND 180);
ALTER TABLE formas_apoyo
    ADD COLUMN icono text NOT NULL DEFAULT 'CORAZON' CHECK (icono IN ('CORAZON', 'LIBRO', 'DINERO')),
    ADD COLUMN orden integer NOT NULL DEFAULT 0;

-- ─── 9. Centros del directorio ───────────────────────────────────────────────
ALTER TABLE centros
    ADD COLUMN informacion     text NOT NULL DEFAULT '',
    ADD COLUMN como_ayudar     text NOT NULL DEFAULT '',
    ADD COLUMN recomendaciones text NOT NULL DEFAULT '',
    ADD COLUMN instagram       text,
    ADD COLUMN ciudad          text NOT NULL DEFAULT 'Monterrey' REFERENCES ciudades (nombre);

-- ─── 10. Campañas ────────────────────────────────────────────────────────────
ALTER TABLE campanas
    ADD COLUMN ciudad            text NOT NULL DEFAULT 'Monterrey' REFERENCES ciudades (nombre),
    ADD COLUMN texto_boton       text NOT NULL DEFAULT 'Quiero ayudar',
    ADD COLUMN telefono          text,
    ADD COLUMN contacto_nombre   text,
    ADD COLUMN meta_texto        text,
    ADD COLUMN descripcion_larga text NOT NULL DEFAULT '',
    ADD COLUMN como_ayudar       text NOT NULL DEFAULT '',
    ADD COLUMN instagram         text,
    ADD COLUMN unidad_meta       text NOT NULL DEFAULT '',
    ADD COLUMN meta_total        integer NOT NULL DEFAULT 0 CHECK (meta_total >= 0),
    ADD COLUMN completados_fuera integer NOT NULL DEFAULT 0 CHECK (completados_fuera >= 0),
    ADD COLUMN latitud           double precision CHECK (latitud BETWEEN -90 AND 90),
    ADD COLUMN longitud          double precision CHECK (longitud BETWEEN -180 AND 180);
ALTER TABLE articulos_meta
    ADD COLUMN apartados_fuera integer NOT NULL DEFAULT 0 CHECK (apartados_fuera >= 0);

CREATE TABLE opciones_donacion (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    campana_id uuid NOT NULL REFERENCES campanas (id) ON DELETE CASCADE,
    nombre     text NOT NULL,
    precio     text NOT NULL,                        -- solo informa: la app no cobra
    orden      integer NOT NULL DEFAULT 0
);
CREATE TABLE puntos_entrega (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    campana_id uuid NOT NULL REFERENCES campanas (id) ON DELETE CASCADE,
    direccion  text NOT NULL,
    colonia    text NOT NULL DEFAULT '',
    orden      integer NOT NULL DEFAULT 0
);
CREATE INDEX aportaciones_articulo_idx ON aportaciones (articulo_id);

-- ─── 11. Sugerencias y privacidad ────────────────────────────────────────────
ALTER TABLE sugerencias
    ADD COLUMN detalle_extra  text NOT NULL DEFAULT '',
    ADD COLUMN organizacion   text NOT NULL DEFAULT '',
    ADD COLUMN contacto_causa text NOT NULL DEFAULT '',
    ADD COLUMN revisada_en    timestamptz;
ALTER TABLE solicitudes_eliminacion ADD COLUMN atendida_en timestamptz;

-- ─── 12. Bitácora de auditoría (OWASP A09) ───────────────────────────────────
-- Quién hizo qué cosa sensible y cuándo. La API solo puede AGREGAR renglones: no puede
-- cambiarlos ni borrarlos (ver afterMigrate.sql), así que un atacante que tome la API
-- no puede borrar sus huellas desde ahí.
CREATE TABLE auditoria (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ocurrio_en  timestamptz NOT NULL DEFAULT now(),
    actor_id    uuid,                -- sin FK a propósito: la bitácora sobrevive a la cuenta
    accion      text NOT NULL,
    objeto_tipo text NOT NULL,
    objeto_id   text,
    detalle     jsonb NOT NULL DEFAULT '{}',
    ip_hmac     text                 -- HMAC-SHA256 de la IP, no la IP
);
CREATE INDEX auditoria_fecha_idx ON auditoria (ocurrio_en DESC);
