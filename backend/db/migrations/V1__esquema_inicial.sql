-- Esquema inicial de Familias que Suman.
--
-- Sale del modelo de la app (domain/Modelos.kt y domain/TestimoniosYEncuestas.kt) y de los
-- requisitos RF-xx. Las migraciones de Flyway no se editan una vez que alguien las aplicó:
-- un cambio nuevo va en el siguiente archivo (V2__lo_que_cambia.sql).
--
-- Convenciones: tablas y columnas en español sin acentos, llaves uuid, fechas con zona horaria
-- (timestamptz), textos de catálogo con CHECK en vez de enums para poder cambiarlos fácil.

CREATE EXTENSION IF NOT EXISTS citext;    -- correos sin distinguir mayúsculas
CREATE EXTENSION IF NOT EXISTS pgcrypto;  -- crypt() para las contraseñas de prueba del seed

-- ─────────────────────────────────────────────────────────────────────────────
-- Cuentas (RF-18) y lo que cuelga de ellas
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE cuentas (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    correo          citext NOT NULL UNIQUE,
    contrasena_hash text   NOT NULL,                      -- bcrypt; nunca la contraseña en claro
    rol             text   NOT NULL DEFAULT 'familia' CHECK (rol IN ('familia', 'admin')),
    nombre_familia  text   NOT NULL,
    ciudad          text   NOT NULL DEFAULT 'Monterrey',
    creada_en       timestamptz NOT NULL DEFAULT now()
);

-- La persona titular y sus acompañantes. Con la fecha de nacimiento la edad se calcula;
-- mientras no se tenga, se guarda la edad que dijeron.
CREATE TABLE acompanantes (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id        uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    nombre           text NOT NULL,
    fecha_nacimiento date,
    edad             smallint CHECK (edad BETWEEN 0 AND 120),
    es_titular       boolean NOT NULL DEFAULT false,
    CONSTRAINT acompanante_edad_o_fecha CHECK (edad IS NOT NULL OR fecha_nacimiento IS NOT NULL)
);
CREATE INDEX acompanantes_cuenta_idx ON acompanantes (cuenta_id);
CREATE UNIQUE INDEX acompanantes_un_titular_por_cuenta ON acompanantes (cuenta_id) WHERE es_titular;

-- Ajustes de notificaciones.
CREATE TABLE preferencias (
    cuenta_id               uuid PRIMARY KEY REFERENCES cuentas (id) ON DELETE CASCADE,
    recordatorio_actividades boolean NOT NULL DEFAULT true,
    avisos_favoritos        boolean NOT NULL DEFAULT true,
    urgencias_ciudad        boolean NOT NULL DEFAULT false
);

-- Teléfonos a los que se les manda push (Firebase Cloud Messaging).
CREATE TABLE dispositivos (
    id        uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    token_fcm text NOT NULL UNIQUE,
    creado_en timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX dispositivos_cuenta_idx ON dispositivos (cuenta_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- Catálogo: asociaciones, actividades, campañas, proyectos y centros
-- Las imágenes se guardan como "clave" del objeto en el almacenamiento S3, no como archivo.
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE asociaciones (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      text NOT NULL,
    categoria   text NOT NULL,
    descripcion text NOT NULL DEFAULT '',
    direccion   text NOT NULL DEFAULT '',
    telefono    text NOT NULL DEFAULT '',
    whatsapp    text NOT NULL DEFAULT '',
    correo      text NOT NULL DEFAULT '',
    ciudad      text NOT NULL DEFAULT 'Monterrey'
);

CREATE TABLE actividades (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asociacion_id      uuid NOT NULL REFERENCES asociaciones (id),
    titulo             text NOT NULL,
    descripcion        text NOT NULL DEFAULT '',
    inicia_en          timestamptz NOT NULL,
    termina_en         timestamptz,
    direccion          text NOT NULL DEFAULT '',
    municipio          text NOT NULL DEFAULT '',
    edad_minima        smallint CHECK (edad_minima >= 0),   -- null = sin restricción de edad
    cupo_total         integer NOT NULL DEFAULT 0 CHECK (cupo_total >= 0),  -- 0 = no publica cupo
    tema               text NOT NULL DEFAULT 'general'
                           CHECK (tema IN ('salud', 'celebracion', 'medio_ambiente', 'general')),
    aportacion_tipo    text NOT NULL DEFAULT 'ninguna'
                           CHECK (aportacion_tipo IN ('ninguna', 'en_especie', 'monetaria')),
    aportacion_monto   text,
    aportacion_detalle text,
    punto_de_encuentro text NOT NULL DEFAULT '',
    acerca_del_proyecto text NOT NULL DEFAULT '',
    que_haremos        text NOT NULL DEFAULT '',
    que_incluye        text NOT NULL DEFAULT '',
    que_llevar         text NOT NULL DEFAULT '',
    recomendaciones    text NOT NULL DEFAULT '',
    foto_clave         text,
    creada_en          timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX actividades_asociacion_idx ON actividades (asociacion_id);
CREATE INDEX actividades_inicia_idx ON actividades (inicia_en);

CREATE TABLE campanas (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asociacion_id    uuid NOT NULL REFERENCES asociaciones (id),
    titulo           text NOT NULL,
    categoria        text NOT NULL DEFAULT '',
    cierra_en        date,                                -- null = sin fecha límite
    urgente          boolean NOT NULL DEFAULT false,
    descripcion      text NOT NULL DEFAULT '',
    descripcion_larga text NOT NULL DEFAULT '',
    como_ayudar      text NOT NULL DEFAULT '',
    unidad_meta      text NOT NULL DEFAULT '',            -- "kits", "despensas", "prendas"
    meta_total       integer NOT NULL DEFAULT 0 CHECK (meta_total >= 0),
    meta_texto       text,                                -- "250-300 cuentos"
    ciudad           text NOT NULL DEFAULT 'Monterrey',
    texto_boton      text NOT NULL DEFAULT 'Quiero ayudar',
    telefono         text,
    whatsapp         text,
    contacto_nombre  text,
    instagram        text,
    imagen_clave     text,
    activa           boolean NOT NULL DEFAULT true,
    creada_en        timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX campanas_asociacion_idx ON campanas (asociacion_id);

-- Lo que se necesita juntar en una campaña y lo que las familias aportan (RF-21).
CREATE TABLE articulos_meta (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    campana_id uuid NOT NULL REFERENCES campanas (id) ON DELETE CASCADE,
    nombre     text NOT NULL,
    meta       integer NOT NULL CHECK (meta > 0)
);
CREATE INDEX articulos_meta_campana_idx ON articulos_meta (campana_id);

CREATE TABLE aportaciones (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id   uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    articulo_id uuid NOT NULL REFERENCES articulos_meta (id) ON DELETE CASCADE,
    cantidad    integer NOT NULL CHECK (cantidad > 0),
    creada_en   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX aportaciones_articulo_idx ON aportaciones (articulo_id);
CREATE INDEX aportaciones_cuenta_idx ON aportaciones (cuenta_id);

-- La app no cobra: estas opciones y puntos solo informan.
CREATE TABLE opciones_donacion (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    campana_id uuid NOT NULL REFERENCES campanas (id) ON DELETE CASCADE,
    nombre     text NOT NULL,
    precio     text NOT NULL                              -- "$700"
);
CREATE INDEX opciones_donacion_campana_idx ON opciones_donacion (campana_id);

CREATE TABLE puntos_entrega (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    campana_id uuid NOT NULL REFERENCES campanas (id) ON DELETE CASCADE,
    direccion  text NOT NULL,
    colonia    text NOT NULL DEFAULT ''
);
CREATE INDEX puntos_entrega_campana_idx ON puntos_entrega (campana_id);

CREATE TABLE proyectos (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre        text NOT NULL,
    descripcion   text NOT NULL DEFAULT '',
    resumen       text NOT NULL DEFAULT '',
    acerca_de     text NOT NULL DEFAULT '',
    beneficiarios text,                                   -- "25 Mujeres"
    ciudad        text NOT NULL DEFAULT 'Monterrey',
    vigencia_hasta date,
    logo_clave    text,
    activo        boolean NOT NULL DEFAULT true,
    como_ayudar   text NOT NULL DEFAULT '',
    telefono      text,
    whatsapp      text,
    instagram     text
);

CREATE TABLE formas_apoyo (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    proyecto_id uuid NOT NULL REFERENCES proyectos (id) ON DELETE CASCADE,
    titulo      text NOT NULL,
    detalle     text NOT NULL DEFAULT '',
    icono       text NOT NULL DEFAULT 'corazon' CHECK (icono IN ('corazon', 'libro', 'dinero')),
    orden       smallint NOT NULL DEFAULT 0
);
CREATE INDEX formas_apoyo_proyecto_idx ON formas_apoyo (proyecto_id);

-- Directorio de visiteo (RF-03).
CREATE TABLE centros_visiteo (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          text NOT NULL,
    tipo            text NOT NULL,                        -- "Asilos", "Casas hogar", "Comedores"
    resumen         text NOT NULL DEFAULT '',
    informacion     text NOT NULL DEFAULT '',
    necesidades     text[] NOT NULL DEFAULT '{}',
    direccion       text NOT NULL DEFAULT '',
    logo_clave      text,
    verificado      boolean NOT NULL DEFAULT true,
    como_ayudar     text NOT NULL DEFAULT '',
    recomendaciones text NOT NULL DEFAULT '',
    telefono        text,
    whatsapp        text,
    instagram       text
);

-- Corazones de favoritos. objeto_id apunta a la tabla que diga "tipo"; sin FK porque
-- apunta a tres tablas distintas.
CREATE TABLE favoritos (
    cuenta_id uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    tipo      text NOT NULL CHECK (tipo IN ('asociacion', 'actividad', 'campana')),
    objeto_id uuid NOT NULL,
    creado_en timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (cuenta_id, tipo, objeto_id)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- Inscripciones (RF-06, RF-19) y asistencias (RF-11)
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE inscripciones (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id    uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    actividad_id uuid NOT NULL REFERENCES actividades (id),
    creada_en    timestamptz NOT NULL DEFAULT now(),
    cancelada_en timestamptz                              -- null = vigente
);
CREATE INDEX inscripciones_actividad_idx ON inscripciones (actividad_id);
CREATE INDEX inscripciones_cuenta_idx ON inscripciones (cuenta_id);
-- Una cuenta solo puede tener una inscripción vigente por actividad.
CREATE UNIQUE INDEX inscripciones_una_vigente ON inscripciones (cuenta_id, actividad_id)
    WHERE cancelada_en IS NULL;

-- Quiénes van: el titular y sus acompañantes. Cada fila ocupa un lugar del cupo.
CREATE TABLE inscripcion_asistentes (
    inscripcion_id uuid NOT NULL REFERENCES inscripciones (id) ON DELETE CASCADE,
    acompanante_id uuid NOT NULL REFERENCES acompanantes (id) ON DELETE CASCADE,
    PRIMARY KEY (inscripcion_id, acompanante_id)
);

-- Lugares ocupados y libres de cada actividad. Esta vista solo informa: al inscribir, la API
-- debe revisar el cupo dentro de una transacción que bloquee la actividad (SELECT ... FOR
-- UPDATE), o dos familias pueden quedarse con el último lugar.
CREATE VIEW actividades_con_cupo AS
SELECT a.*,
       COALESCE(o.ocupados, 0)                               AS lugares_ocupados,
       GREATEST(a.cupo_total - COALESCE(o.ocupados, 0), 0)   AS lugares_disponibles
FROM actividades a
LEFT JOIN (
    SELECT i.actividad_id, count(*) AS ocupados
    FROM inscripciones i
    JOIN inscripcion_asistentes ia ON ia.inscripcion_id = i.id
    WHERE i.cancelada_en IS NULL
    GROUP BY i.actividad_id
) o ON o.actividad_id = a.id;

-- El historial de participación se arma solo con asistencias que registra la asociación,
-- no con lo que la familia declare por su cuenta.
CREATE TABLE asistencias (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id     uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    actividad_id  uuid NOT NULL REFERENCES actividades (id),
    registrada_en timestamptz NOT NULL DEFAULT now(),
    UNIQUE (cuenta_id, actividad_id)
);
CREATE INDEX asistencias_actividad_idx ON asistencias (actividad_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- Testimonios (RF-12) y encuestas (RF-13)
-- ─────────────────────────────────────────────────────────────────────────────

-- Un testimonio por asistencia. Se publica al enviarse; lo elimina su autora o autor, o el
-- admin (la API valida quién). La foto vive en el almacenamiento S3, aquí solo su clave.
CREATE TABLE testimonios (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asistencia_id uuid NOT NULL UNIQUE REFERENCES asistencias (id) ON DELETE CASCADE,
    experiencia   text NOT NULL CHECK (char_length(btrim(experiencia)) BETWEEN 20 AND 500),
    foto_clave    text NOT NULL,
    creado_en     timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX testimonios_creado_idx ON testimonios (creado_en DESC);

-- Las dos encuestas de una actividad (antes y después) cuelgan de la misma actividad, así
-- se pueden comparar para medir la experiencia y el aprendizaje. Cada respuesta guarda la
-- pregunta y la opción tal como se leyeron: [{"numero":1,"pregunta":"...","respuesta":"..."}].
CREATE TABLE respuestas_encuesta (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cuenta_id    uuid NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    actividad_id uuid NOT NULL REFERENCES actividades (id),
    momento      text NOT NULL CHECK (momento IN ('antes', 'despues')),
    respuestas   jsonb NOT NULL CHECK (jsonb_typeof(respuestas) = 'array'),
    creada_en    timestamptz NOT NULL DEFAULT now(),
    UNIQUE (cuenta_id, actividad_id, momento)
);
CREATE INDEX respuestas_encuesta_actividad_idx ON respuestas_encuesta (actividad_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- Sugerencias de las familias y solicitudes de eliminación de datos (HU-14)
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE sugerencias (
    id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo                     text NOT NULL CHECK (tipo IN ('actividad', 'campana', 'proyecto', 'centro')),
    nombre                   text NOT NULL,
    descripcion              text NOT NULL,
    detalle_extra            text NOT NULL DEFAULT '',
    ciudad                   text NOT NULL DEFAULT 'Monterrey',
    organizacion             text NOT NULL DEFAULT '',
    contacto_causa           text NOT NULL DEFAULT '',
    sugerida_por             text NOT NULL,
    contacto_de_quien_sugiere text NOT NULL,
    cuenta_id                uuid REFERENCES cuentas (id) ON DELETE SET NULL,
    estado                   text NOT NULL DEFAULT 'pendiente'
                                 CHECK (estado IN ('pendiente', 'aprobada', 'descartada')),
    creada_en                timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX sugerencias_estado_idx ON sugerencias (estado);

-- La solicitud sobrevive a la cuenta: al eliminarla, cuenta_id queda en null pero el correo
-- y el folio se conservan como constancia. El folio que ve la familia es 'ELIM-' + folio.
CREATE TABLE solicitudes_eliminacion (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    folio       bigint GENERATED ALWAYS AS IDENTITY UNIQUE,
    cuenta_id   uuid REFERENCES cuentas (id) ON DELETE SET NULL,
    correo      citext NOT NULL,
    estado      text NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'atendida')),
    creada_en   timestamptz NOT NULL DEFAULT now(),
    atendida_en timestamptz
);
-- Una sola solicitud pendiente por correo.
CREATE UNIQUE INDEX solicitudes_eliminacion_una_pendiente ON solicitudes_eliminacion (correo)
    WHERE estado = 'pendiente';
