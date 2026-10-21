-- Datos de prueba SOLO para desarrollo (nunca en un servidor real). Es lo mismo que antes vivía
-- en la app (DatosDePrueba.kt), ahora en la base, que es donde va.
--
--   Se cargan solos con `docker compose up -d`, después de las migraciones.
--
-- Se puede correr las veces que sea:
--   · El CATÁLOGO (asociaciones, actividades, campañas, proyectos, centros) se ACTUALIZA cada vez,
--     y las fechas se calculan desde hoy: siempre hay actividades próximas y otras ya pasadas.
--     Si editas una de estas desde la app, el siguiente `up` la regresa como está aquí.
--   · Lo de la FAMILIA DE PRUEBA solo se agrega si no existe: no pisa lo que hagas al probar.
--
-- Cuentas de prueba (SOLO desarrollo; la contraseña se guarda como hash Argon2id, nunca en claro):
--   · familia:        ana.rodriguez@correo.com / familia123
--   · administración: admin@correo.com         / admin123   (con los cuatro permisos)
-- En un servidor real no se carga este archivo: las cuentas se crean desde la app y los permisos
-- se dan con `python -m app.herramientas permisos` (ver docs/guia-backend.md, §4).

\set ON_ERROR_STOP on

-- El sábado (o el día que se pida) de la semana actual más `semanas`, a una hora de Monterrey.
--   dia: 0 = lunes … 5 = sábado, 6 = domingo
CREATE FUNCTION pg_temp.fecha(semanas int, dia int, hora text) RETURNS timestamptz
LANGUAGE sql STABLE AS $$
    SELECT ((date_trunc('week', now() AT TIME ZONE 'America/Monterrey')::date + dia + 7 * semanas)::text || ' ' || hora)::timestamp
           AT TIME ZONE 'America/Monterrey'
$$;

-- ─── Asociaciones del directorio y quienes organizan actividades ────────────
INSERT INTO asociaciones (id, nombre, categoria, descripcion, direccion, telefono, whatsapp, correo, en_directorio) VALUES
  ('b0000000-0000-0000-0000-000000000011', 'Comedor Comunitario San Bernabé', 'Alimentación',
   'Damos comida caliente a 180 familias de la colonia y armamos despensas de fin de mes.',
   'Av. Rómulo Garza 240, Col. San Bernabé, Monterrey', '8100000001', '5218100000001', 'contacto@comedorsanbernabe.org', true),
  ('b0000000-0000-0000-0000-000000000012', 'Casa de Día Los Robles', 'Adultos mayores',
   'Acompañamos a 40 abuelitos con actividades dos veces por semana.',
   'Calle Los Robles 118, Monterrey', '8100000002', '5218100000002', 'contacto@casalosrobles.org', true),
  ('b0000000-0000-0000-0000-000000000013', 'Albergue Nuevo Amanecer', 'Ropa y abrigo',
   'Recibimos ropa de invierno todo el año para personas en situación de calle.',
   'Av. Colón 905, Monterrey', '8100000003', '5218100000003', 'contacto@nuevoamanecer.org', true),
  ('b0000000-0000-0000-0000-000000000014', 'Parroquia San Bernabé', 'Niñez',
   'Organizamos la primera comunión de los niños de la colonia.',
   'Av. Aztlán 1500, Monterrey', '8100000004', '5218100000004', 'contacto@parroquiasanbernabe.org', true),
  ('b0000000-0000-0000-0000-000000000021', 'Familias que Suman', 'Voluntariado familiar',
   'Conectamos familias con oportunidades de voluntariado en su ciudad.',
   'Monterrey, N.L.', '8100000005', '5218100000005', 'hola@familiasquesuman.com', false),
  ('b0000000-0000-0000-0000-000000000001', 'Regalando Estrellas', 'Salud',
   'Desde hace 11 años visitamos el Hospital Materno Infantil.',
   'Guadalupe, N.L.', '8100000006', '5218100000006', 'contacto@regalandoestrellas.org', false),
  ('b0000000-0000-0000-0000-000000000002', 'Cíclica', 'Medio ambiente',
   'Organizamos la Mega Limpieza con familias de la ciudad.',
   'Monterrey, N.L.', '8100000007', '5218100000007', 'contacto@ciclica.org', false)
ON CONFLICT (id) DO UPDATE SET
  nombre = EXCLUDED.nombre, categoria = EXCLUDED.categoria, descripcion = EXCLUDED.descripcion,
  direccion = EXCLUDED.direccion, telefono = EXCLUDED.telefono, whatsapp = EXCLUDED.whatsapp,
  correo = EXCLUDED.correo, en_directorio = EXCLUDED.en_directorio;

-- ─── Actividades (las de familiasquesuman.com/actividades) ──────────────────
INSERT INTO actividades (id, asociacion_id, titulo, descripcion, inicia_en, termina_en, direccion, cupo_total, ocupados_fuera,
                         edad_minima, municipio, tema, aportacion_tipo, aportacion_monto, aportacion_detalle, punto_encuentro,
                         acerca_del_proyecto, que_haremos, que_incluye, que_llevar, recomendaciones, imagen, latitud, longitud) VALUES
  -- Próximas
  ('d0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000021',
   'Regalando Estrellas Visita al Materno Infantil',
   'Entrega de aproximadamente 200 desayunos. Entrega de regalos, juguetes y kits de higiene personal.',
   pg_temp.fecha(1, 5, '09:45'), pg_temp.fecha(1, 5, '11:30'), 'Av. San Rafael No. 450', 5, 5,
   7, 'Guadalupe', 'SALUD', 'especie', '',
   'Se solicita: plátanos, manzanas, mandarinas, sándwiches, galletas o paletas heladas. Es necesario ver en esta visita qué es lo que hace falta. Cada familia voluntaria llega con una aportación.',
   'Explanada del Hospital Materno Infantil',
   'Desde hace ya 11 años se hacen visitas al Hospital Materno Infantil, se regalan 200 desayunos, se visita a los enfermos y se regalan kits de higiene personal a las mujeres que se acaban de convertir en mamás.',
   E'Entrega de aproximadamente 200 desayunos.\nEntrega de regalos, juguetes y kits de higiene personal.', '',
   'Es necesario comunicarse con la encargada de Regalando Estrellas para organizar tu aportación.',
   E'Pueden asistir familias con niños mayores de 7 años.\nLlevar ropa cómoda, blusa blanca, sencilla, tenis.\nContáctate con Regalando Estrellas si tienes más dudas.\nNo olvides comentar que eres una Familia que Suma+',
   'actividad_regalando_estrellas', 25.6919, -100.2163),
  ('d0000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-000000000011',
   'Preparar despensas de fin de mes',
   'Vamos a armar 300 despensas para las familias de la colonia. Se forman equipos de cuatro. Lleven ropa cómoda y agua.',
   pg_temp.fecha(1, 5, '09:00'), pg_temp.fecha(1, 5, '12:00'), 'Av. Rómulo Garza 240, Col. San Bernabé', 20, 12,
   6, 'Monterrey', 'GENERAL', 'ninguna', '', '', 'Comedor Comunitario San Bernabé',
   '', 'Armar despensas en equipos de cuatro.', '', '', 'Ropa cómoda y agua.', NULL, 25.7480, -100.3620),
  ('d0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000012',
   'Tarde de lectura con abuelitos',
   'Cada familia lee un cuento con un abuelito y después compartimos un café.',
   pg_temp.fecha(1, 6, '16:00'), pg_temp.fecha(1, 6, '18:00'), 'Calle Los Robles 118', 12, 10,
   4, 'Monterrey', 'GENERAL', 'ninguna', '', '', 'Casa de Día Los Robles',
   '', 'Leer un cuento con un abuelito y compartir un café.', '', '', '', NULL, 25.6510, -100.3350),
  ('d0000000-0000-0000-0000-000000000011', 'b0000000-0000-0000-0000-000000000013',
   'Clasificar ropa de invierno',
   'Separamos la ropa donada por talla y tipo antes de repartirla.',
   pg_temp.fecha(2, 5, '10:00'), pg_temp.fecha(2, 5, '13:00'), 'Av. Colón 905', 25, 10,
   15, 'Monterrey', 'GENERAL', 'ninguna', '', '', 'Albergue Nuevo Amanecer',
   '', 'Separar la ropa donada por talla y tipo.', '', '', '', NULL, 25.6870, -100.3220),
  ('d0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000021',
   'Posada Sendero',
   'Posada navideña para las familias del Arroyo en la Colonia Sendero.',
   pg_temp.fecha(6, 5, '09:30'), pg_temp.fecha(6, 5, '12:00'), 'San Ildefonso SN, Colonia Sendero, Santa Catarina', 15, 6,
   NULL, 'Monterrey', 'CELEBRACION', 'especie', '', '', 'Centro Comunitario',
   'Llevamos una mañana de regalos y juegos a las familias del Arroyo en la Colonia Sendero.',
   'Juegos, regalos, merienda y convivencia.', '',
   'Se junta una cantidad previa para hacer pagos de flautas, juguetes, pasteles, etc.',
   'Ropa cómoda y sencilla.', 'actividad_posada_sendero', 25.6830, -100.4720),
  -- Ya pasaron
  ('d0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002',
   'Mega Limpieza San Pedro',
   'Actividad familiar para ayudar haciendo limpieza al medio ambiente.',
   pg_temp.fecha(-2, 5, '08:00'), pg_temp.fecha(-2, 5, '10:30'), 'Humberto Lobo esq. con Av. Ignacio Morones Prieto', 0, 0,
   NULL, 'Monterrey', 'MEDIO_AMBIENTE', 'ninguna', '', '', 'Auditorio San Pedro',
   'Actividad familiar para ayudar a hacer limpieza de áreas.', 'Limpieza de áreas.',
   'Guantes, chalecos, herramientas e hidratación.', 'Bloqueador, zapato cerrado, gorra, termo de agua.',
   E'Llevar termo con agua, jeans, gorra, protector solar y actitud.\nVisitar la página www.megalimpieza.org',
   'actividad_mega_limpieza', NULL, NULL),
  ('d0000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000021',
   'Regalando Estrellas Visita al Materno Infantil',
   'Entrega de aproximadamente 200 desayunos. Entrega de regalos, juguetes y kits de higiene personal.',
   pg_temp.fecha(-5, 5, '09:45'), pg_temp.fecha(-5, 5, '11:30'), 'Av. San Rafael No. 450', 9, 9,
   7, 'Guadalupe', 'SALUD', 'especie', '',
   'Se solicita: plátanos, manzanas, mandarinas, sándwiches, galletas o paletas heladas. Es necesario ver en esta visita qué es lo que hace falta. Cada familia voluntaria llega con una aportación.',
   'Explanada del Hospital Materno Infantil',
   'Desde hace ya 11 años se hacen visitas al Hospital Materno Infantil, se regalan 200 desayunos, se visita a los enfermos y se regalan kits de higiene personal a las mujeres que se acaban de convertir en mamás.',
   E'Entrega de aproximadamente 200 desayunos.\nEntrega de regalos, juguetes y kits de higiene personal.', '',
   'Es necesario comunicarse con la encargada de Regalando Estrellas para organizar tu aportación.',
   E'Pueden asistir familias con niños mayores de 7 años.\nLlevar ropa cómoda, blusa blanca, sencilla, tenis.\nContáctate con Regalando Estrellas si tienes más dudas.\nNo olvides comentar que eres una Familia que Suma+',
   'actividad_regalando_estrellas', NULL, NULL),
  ('d0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000001',
   'Regalando Estrellas Visita al Materno Infantil',
   'Entrega de aproximadamente 200 desayunos. Entrega de regalos, juguetes y kits de higiene personal.',
   pg_temp.fecha(-9, 5, '09:45'), pg_temp.fecha(-9, 5, '11:30'), 'Av. San Rafael No. 450', 20, 3,
   NULL, 'Guadalupe', 'SALUD', 'ninguna', '', '', 'Explanada del Hospital Materno Infantil',
   'Desde hace ya 11 años se hacen visitas al Hospital Materno Infantil, se regalan 200 desayunos, se visita a los enfermos y se regalan kits de higiene personal a las mujeres que se acaban de convertir en mamás.',
   E'Entrega de aproximadamente 200 desayunos.\nEntrega de regalos, juguetes y kits de higiene personal.', '',
   'Es necesario comunicarse con la encargada de Regalando Estrellas para organizar tu aportación.',
   E'Pueden asistir familias completas.\nLlevar ropa cómoda, sencilla, tenis.\nContáctate con Regalando Estrellas si tienes más dudas.\nNo olvides comentar que eres una Familia que Suma+',
   'actividad_regalando_estrellas', NULL, NULL),
  ('d0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000002',
   'Mega Limpieza', 'Actividad familiar para ayudar al medio ambiente.',
   pg_temp.fecha(-13, 5, '08:00'), pg_temp.fecha(-13, 5, '10:30'), '', 0, 0,
   NULL, 'Monterrey', 'MEDIO_AMBIENTE', 'ninguna', '', '', '',
   'Actividad familiar para ayudar a hacer limpieza de áreas.', 'Limpieza de áreas.',
   'Guantes, chalecos, herramientas e hidratación.', '',
   E'Llevar termo con agua, jeans, gorra, protector solar y actitud.\nVisitar la página www.megalimpieza.org',
   'actividad_mega_limpieza', NULL, NULL),
  ('d0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000001',
   'Regalando Estrellas Visita al Materno Infantil',
   'Entrega de aproximadamente 200 desayunos. Entrega de regalos, juguetes y kits de higiene personal.',
   pg_temp.fecha(-13, 5, '09:45'), pg_temp.fecha(-13, 5, '11:30'), 'Av. San Rafael No. 450', 0, 0,
   NULL, 'Guadalupe', 'SALUD', 'ninguna', '', '', 'Explanada del Hospital Materno Infantil',
   'Desde hace ya 11 años se hacen visitas al Hospital Materno Infantil, se regalan 200 desayunos, se visita a los enfermos y se regalan kits de higiene personal a las mujeres que se acaban de convertir en mamás.',
   E'Entrega de aproximadamente 200 desayunos.\nEntrega de regalos, juguetes y kits de higiene personal.', '',
   'Si tienes oportunidad, aporta algo de frutas como manzanas, plátanos o naranjas.',
   E'Pueden asistir familias completas.\nLlevar ropa cómoda, sencilla, tenis.\nContáctate con Regalando Estrellas si tienes más dudas.\nNo olvides comentar que eres una Familia que Suma+',
   'actividad_regalando_estrellas', NULL, NULL),
  ('d0000000-0000-0000-0000-000000000013', 'b0000000-0000-0000-0000-000000000021',
   'Festejo a los papás de Apadrina un niño',
   '5 familias completas. Celebra a los papás de Apadrina un niño. Juegos y convivencia.',
   pg_temp.fecha(-16, 5, '10:00'), pg_temp.fecha(-16, 5, '12:00'), '', 0, 0,
   NULL, 'Monterrey', 'CELEBRACION', 'monetaria', '$500', 'Contactarse para hacer el pago a través de Moneypool.', '',
   'Convivencia de 10 familias para festejar a los papás que luchan todos los días por la salud de sus hijos.',
   'Jugar lotería y convivir.', 'Con la aportación compraremos algo para compartir de alimentos y bebidas.', '', '',
   NULL, NULL, NULL)
ON CONFLICT (id) DO UPDATE SET
  asociacion_id = EXCLUDED.asociacion_id, titulo = EXCLUDED.titulo, descripcion = EXCLUDED.descripcion,
  inicia_en = EXCLUDED.inicia_en, termina_en = EXCLUDED.termina_en, direccion = EXCLUDED.direccion,
  cupo_total = EXCLUDED.cupo_total, ocupados_fuera = EXCLUDED.ocupados_fuera, edad_minima = EXCLUDED.edad_minima,
  municipio = EXCLUDED.municipio, tema = EXCLUDED.tema, aportacion_tipo = EXCLUDED.aportacion_tipo,
  aportacion_monto = EXCLUDED.aportacion_monto, aportacion_detalle = EXCLUDED.aportacion_detalle,
  punto_encuentro = EXCLUDED.punto_encuentro, acerca_del_proyecto = EXCLUDED.acerca_del_proyecto,
  que_haremos = EXCLUDED.que_haremos, que_incluye = EXCLUDED.que_incluye, que_llevar = EXCLUDED.que_llevar,
  recomendaciones = EXCLUDED.recomendaciones, imagen = EXCLUDED.imagen, latitud = EXCLUDED.latitud, longitud = EXCLUDED.longitud;

-- ─── Campañas (las de familiasquesuman.com/donar) ───────────────────────────
INSERT INTO campanas (id, titulo, categoria, cierra_en, urgente, descripcion, imagen_clave, texto_boton, telefono, whatsapp,
                      contacto_nombre, meta_texto, descripcion_larga, como_ayudar, instagram, unidad_meta, meta_total,
                      completados_fuera, latitud, longitud) VALUES
  ('f1000000-0000-0000-0000-000000000002', 'Destellos de Luz', 'Salud', NULL, false,
   'Destellos de Luz A.B.P. cuenta con más de 29 años de experiencia brindando atención oftalmológica integral y programas educativos que impulsan la autonomía y calidad de vida de personas con discapacidad visual.',
   'campana_destellos', 'Quiero ayudar', '8180924504', '8180924504', 'Mariana Báez', NULL,
   E'Destellos de Luz A.B.P. es una asociación con más de 29 años de trayectoria, que cuenta con un área médica y un área educativa.\n\nEn el área Médica: brindamos atención oftalmológica integral, respaldada por un cuerpo médico altruista especializado, desde la valoración médica hasta cirugías y tratamientos especializados, para quienes más lo necesitan.\n\nEn el área Educativa: a través de 7 programas de educación fortalecemos las habilidades de personas con discapacidad visual para favorecer su superación y mejorar su calidad de vida.',
   'Aporta herramientas para personas con discapacidad visual las cuales les facilitan su desplazamiento y aprendizaje.',
   'https://www.instagram.com/destellosdeluzabp/', '', 0, 0, 25.6760, -100.3340),
  ('f1000000-0000-0000-0000-000000000003', 'Tapitas que Suman', '', make_date(extract(year FROM now())::int, 12, 30), false,
   'Recolección de tapitas de plástico para reciclaje', 'campana_tapitas', 'Quiero juntar', '8110809078', '8110809078',
   'Familias que Suman', NULL,
   'Junta en casa todas las tapitas de plástico para reciclaje y ayuda a la alimentación y el medio ambiente.',
   'Ayudanos a difundir entre familiares y amigos y invitalos a juntar todas las tapitas de botellas de plástico. Se pueden reciclar, se reutiliza ese plástico y con ese dinero recaudado podemos ayudar a niños con cáncer y a sus familias.',
   NULL, '', 0, 0, 25.6520, -100.2890),
  ('f1000000-0000-0000-0000-000000000004', 'Suma a su Mesa', 'Alimentos', (now() AT TIME ZONE 'America/Monterrey')::date + 17, false,
   'Recolección de alimentos y productos de limpieza para el Asilo de Ancianos Morada del Anciano Desvalido de Cadereyta. Se sirven 6,900 servicios de alimentación al mes.',
   'campana_suma_mesa', 'Quiero ayudar', '8114707350', '8114707350', 'Adriana Guerra', NULL,
   'Recolección de alimentos no perecederos y productos de limpieza para el Asilo con Adultos Mayores en Cadereyta. Alimentos: aceite, arroz, frijoles, lentejas, azúcar, avena, atún, productos enlatados, leche, té, café, galletas, gelatina, pasta, saladitas, servilletas, papel de baño. Productos de limpieza: escobas, trapeador, bolsas de basura, cloro, fabuloso, pinol, esponjas, jabón liquido, desengrasante.',
   'Recolecta entre familia y amigos alimentos y productos de limpieza. Existen 3 puntos de entrega. Reúne entre tu comunidad y hagan una gran aportación.',
   NULL, '', 0, 0, 25.6560, -100.3690),
  ('f1000000-0000-0000-0000-000000000001', 'Bibliotecas Infantiles', 'Útiles escolares', NULL, false,
   'Recolección de cuentos infantiles para crear Bibliotecas', 'campana_bibliotecas', 'Quiero ayudar', '8120322281', NULL,
   'Familias que Suman', '250-300 cuentos',
   'Reúne cuentos infantiles para crear Bibliotecas para niños. No diccionarios, no libros de texto y no libros de colorear. Se necesitan entre 250 y 300 cuentos para juntar la Biblioteca.',
   'Junta cuentos desde preescolar hasta secundaria en buen estado.', NULL, 'cuentos', 300, 0, 25.6400, -100.3100)
ON CONFLICT (id) DO UPDATE SET
  titulo = EXCLUDED.titulo, categoria = EXCLUDED.categoria, cierra_en = EXCLUDED.cierra_en, urgente = EXCLUDED.urgente,
  descripcion = EXCLUDED.descripcion, imagen_clave = EXCLUDED.imagen_clave, texto_boton = EXCLUDED.texto_boton,
  telefono = EXCLUDED.telefono, whatsapp = EXCLUDED.whatsapp, contacto_nombre = EXCLUDED.contacto_nombre,
  meta_texto = EXCLUDED.meta_texto, descripcion_larga = EXCLUDED.descripcion_larga, como_ayudar = EXCLUDED.como_ayudar,
  instagram = EXCLUDED.instagram, unidad_meta = EXCLUDED.unidad_meta, meta_total = EXCLUDED.meta_total,
  completados_fuera = EXCLUDED.completados_fuera, latitud = EXCLUDED.latitud, longitud = EXCLUDED.longitud;

-- Las partes de cada campaña de prueba se reemplazan completas (son solo del catálogo).
DELETE FROM opciones_donacion WHERE campana_id = 'f1000000-0000-0000-0000-000000000002';
INSERT INTO opciones_donacion (campana_id, nombre, precio, orden) VALUES
  ('f1000000-0000-0000-0000-000000000002', 'Bastón para desplazamiento', '$700', 0),
  ('f1000000-0000-0000-0000-000000000002', 'Computadora parlante', '$21,000', 1),
  ('f1000000-0000-0000-0000-000000000002', 'Cirugía', '$14,000', 2);

DELETE FROM puntos_entrega WHERE campana_id = 'f1000000-0000-0000-0000-000000000004';
INSERT INTO puntos_entrega (campana_id, direccion, colonia, orden) VALUES
  ('f1000000-0000-0000-0000-000000000004', 'Calle Cóndor 1001', 'Fraccionamiento Azhara', 0),
  ('f1000000-0000-0000-0000-000000000004', 'Río Amazonas 327', 'Del Valle, 66220 San Pedro Garza García', 1),
  ('f1000000-0000-0000-0000-000000000004', 'Fuego 45', 'Colonia Olinca', 2);

-- 300 cuentos: 90 apartados por fuera de la app + los 10 de la familia de prueba (abajo) = 100.
INSERT INTO articulos_meta (id, campana_id, nombre, meta, apartados_fuera) VALUES
  ('f2000000-0000-0000-0000-000000000001', 'f1000000-0000-0000-0000-000000000001', 'Cuentos infantiles', 300, 90)
ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre, meta = EXCLUDED.meta, apartados_fuera = EXCLUDED.apartados_fuera;

-- ─── Proyectos (familiasquesuman.com/proyectos) ─────────────────────────────
INSERT INTO proyectos (id, nombre, resumen, descripcion, beneficiarios, ciudad, activo, telefono, whatsapp, logo_clave,
                       vigencia, acerca_de, como_ayudar, instagram, latitud, longitud) VALUES
  ('e0000000-0000-0000-0000-000000000001', 'Trazo... Escribiendo una nueva historia',
   'Voluntariado que visita a mujeres en el penal de Escobedo.',
   E'Somos un grupo de mujeres voluntarias que realizamos visitas quincenales al Centro de Reinserción Social de Escobedo para acompañar a mujeres privadas de la libertad mediante pláticas de desarrollo humano, talleres de acuarela y actividades que fortalecen su autoestima, creatividad y crecimiento personal.\n\nEn cada encuentro buscamos ofrecer un espacio de escucha, aprendizaje y esperanza, recordándoles que siempre es posible comenzar de nuevo.',
   '25 Mujeres', 'Monterrey', true, '8110771068', '8110771068', 'proyecto_trazo', NULL,
   'Trazo es un voluntariado que nace de la Asociación Renace, desde hace más de 2 años lleva pláticas de desarrollo humano y cursos de acuarela y caligrafía a mujeres privadas de la libertad quienes al final del semestre reciben un reconocimiento el cual les ayuda en su expediente judicial.',
   '', NULL, 25.7930, -100.3200),
  ('e0000000-0000-0000-0000-000000000002', 'Voluntariado DIF Te Acompaña', 'Acompañar a personas vulnerables.',
   'Acompañar a adultos mayores o jóvenes vulnerables, en soledad, con discapacidad o con una red de apoyo reducida. Realizar visitas en familia mínimo una vez cada 15 días. Se les puede asistir en compras de alimentos, medicamentos o solo una visita.',
   '200 adultos mayores', 'San Pedro Garza García', true, '8113008725', '8113008725', 'proyecto_dif', NULL,
   'El voluntariado existe desde hace varios años y se busca apoyar a personas vulnerables con una visita o ayuda con alimentos, medicinas o acompañamiento.',
   'Inscribir a su familia para que el Voluntariado les asigne una persona para visitar o acompañar.', NULL, 25.6730, -100.3050),
  ('e0000000-0000-0000-0000-000000000003', 'Cocinando de Corazón a Corazón',
   'Comedor comunitario de alimentos preparados por una red de amas de casa que regalan su tiempo preparando alimentos desde sus hogares.',
   E'Comedor comunitario diocesano que lleva alimento y esperanza a personas y familias en situación vulnerable. A través de una red de parroquias, los alimentos preparados se distribuyen en distintas comunidades, permitiendo llegar de manera cercana y organizada a quienes más lo necesitan.\n\nEsta obra se sostiene gracias a la generosidad de benefactores, voluntarios y, de manera muy especial, de una gran red de amas de casa que regalan su tiempo y talento preparando los alimentos con cariño desde sus hogares.',
   NULL, 'Monterrey', true, '8184596229', '8184596229', 'proyecto_cocinando', NULL,
   'Desde hace 6 años un conjunto de amas de casa comenzaron a apoyar cocinando desde su casa para después repartir los alimentos en comunidades vulnerables. El proyecto creció y actualmente se entregan hasta 1000 platillos diarios.',
   '', 'https://www.instagram.com/cocinando_de_corazon/', 25.7100, -100.3400),
  ('e0000000-0000-0000-0000-000000000004', 'Forma-T',
   'Asociación que busca provocar en la comunidad una formación de valores y talentos para ponerlos al servicio de los demás.',
   'Clases impartidas por voluntarias en escuelas públicas enfocadas en la formación en valores, escuela para padres y apoyo académico para niños y jóvenes.',
   NULL, 'Monterrey', true, '8180201207', '8180201207', 'proyecto_forma_t', NULL,
   'Desde el 2016 se ofrece apoyo académico impartido por voluntarias a alumnos de escuelas públicas con rezago escolar. Escuela para padres y formación en valores para familias y alumnos.',
   '', 'https://www.instagram.com/format_mty', 25.6650, -100.3550),
  ('e0000000-0000-0000-0000-000000000005', 'Materno Infantil - REGALANDO ESTRELLAS', 'Visita una vez al mes el Hospital Materno Infantil.',
   'Visita al Hospital Materno Infantil el primer sábado al mes junto con más familias. Se sirven aproximadamente 200 desayunos y se llevan juguetes o kits de higiene personal.',
   '1500 Bebés, mamás, maternidad', 'Monterrey', true, '8134021445', '8134021445', 'actividad_regalando_estrellas', NULL,
   'Desde hace ya 11 años se hacen visitas al Hospital Materno Infantil, se regalan 200 desayunos, se visita a los enfermos y se regalan kits de higiene personal a las mujeres que se acaban de convertir en mamás.',
   '', 'https://www.instagram.com/regalandoestrellas.mx/', 25.6931, -100.2148),
  ('e0000000-0000-0000-0000-000000000006', 'Bibliotecas Infantiles - Familia Viva', 'Bibliotecas para entregar en colegios o instituciones infantiles.',
   'Recolección de cuentos infantiles de preescolar, primaria y secundaria para crear una biblioteca infantil que se entregará a instituciones con niños. Se pueden entregar en albergues, casas hogar, centros comunitarios y escuelas.',
   NULL, 'Monterrey', true, '8180296174', '8180296174', 'proyecto_bibliotecas', NULL,
   'Familia Viva detectó la necesidad de contar con más libros y espacios de lectura en escuelas públicas. A partir de ahí nació una campaña para recolectar y entregar libros y cuentos infantiles. En 2025, gracias al esfuerzo conjunto de Alas MX, Familia Viva y muchas familias voluntarias, se han logrado entregar hasta hoy 38 salitas de lectura en escuelas públicas. Es un proyecto sustentable porque tanto los libros como los muebles son reutilizados.',
   'Recolectar cuentos o libros desde preescolar, primaria y secundaria. Conseguir un librero o comprar uno en alguna tienda tipo Home Depot. Una vez reunidos los 250 o 300 libros se clasifican y se arma la biblioteca. No diccionarios, no libros de texto y no libros de colorear. Se pueden organizar con la encargada del proyecto para clasificar libros e irla a entregar personalmente.',
   'https://www.instagram.com/familiaviva.mx/', 25.6480, -100.3200),
  ('e0000000-0000-0000-0000-000000000007', 'Sendero - REGALANDO ESTRELLAS', 'Pláticas y convivencia con mujeres de comunidad vulnerable.',
   'Pláticas y convivencia con mujeres en situación vulnerable donde les pueden enseñar, convivir y compartir aprendizajes.',
   NULL, 'Monterrey', true, '8134021445', '8134021445', 'actividad_regalando_estrellas', NULL,
   'Solo mujeres. Pláticas, manualidades y enseñanza a mujeres vulnerables. Último sábado del mes. 10:00 am a 12:00 pm.',
   'Hacer una visita en grupo a una comunidad donde hay muchas mujeres con quienes platicar, convivir y compartir aprendizajes.',
   NULL, 25.6842, -100.4698),
  ('e0000000-0000-0000-0000-000000000008', 'Escucha Corazón', 'Acompañamiento a alumnas de primaria y secundaria.',
   'Una visita al mes al colegio Mano Amiga Monterrey en donde darás acompañamiento a alumnas de primaria y secundaria.',
   NULL, 'Monterrey', true, '8110809089', '8110809078', 'proyecto_escucha_corazon', 'Hasta 29 jun 2026',
   'Mínimo 4 visitas en el ciclo escolar. Tiempo aprox. 25 minutos con cada alumna. Pueden ser desde 2 a 4 alumnas en el ciclo escolar. Horario y días flexibles.',
   'Dar acompañamiento ayuda a las alumnas a ser escuchadas.', NULL, 25.7000, -100.2800)
ON CONFLICT (id) DO UPDATE SET
  nombre = EXCLUDED.nombre, resumen = EXCLUDED.resumen, descripcion = EXCLUDED.descripcion,
  beneficiarios = EXCLUDED.beneficiarios, ciudad = EXCLUDED.ciudad, activo = EXCLUDED.activo,
  telefono = EXCLUDED.telefono, whatsapp = EXCLUDED.whatsapp, logo_clave = EXCLUDED.logo_clave,
  vigencia = EXCLUDED.vigencia, acerca_de = EXCLUDED.acerca_de, como_ayudar = EXCLUDED.como_ayudar,
  instagram = EXCLUDED.instagram, latitud = EXCLUDED.latitud, longitud = EXCLUDED.longitud;

DELETE FROM formas_apoyo WHERE proyecto_id IN (
  'e0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000003',
  'e0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000005');
INSERT INTO formas_apoyo (proyecto_id, titulo, detalle, icono, orden) VALUES
  ('e0000000-0000-0000-0000-000000000001', 'Buscamos personas que quieran compartir su tiempo y talento.',
   'Voluntarias que imparten pláticas de desarrollo humano. • Talleres de acuarela y actividades creativas. • Otros talleres que promuevan el aprendizaje y el bienestar emocional.', 'LIBRO', 0),
  ('e0000000-0000-0000-0000-000000000001', 'Aportación económica',
   'Para comprar materiales: • Pinturas, pinceles, papel y otros insumos. • Aportaciones económicas para los alimentos que se entregan a las mujeres privadas de la libertad durante cada visita.', 'DINERO', 1),
  ('e0000000-0000-0000-0000-000000000003', 'Cocinando desde casa', 'Elige un día fijo a la semana y cocina un mínimo de 20 platillos.', 'CORAZON', 0),
  ('e0000000-0000-0000-0000-000000000003', 'Apoya al equipo del comedor', 'Elige un día fijo a la semana y apoya al comedor sirviendo y emplatando comidas.', 'CORAZON', 1),
  ('e0000000-0000-0000-0000-000000000003', 'Aporta alimentos', 'Aporta cualquier tipo de alimento para que otras familias puedan cocinar desde sus casas.', 'CORAZON', 2),
  ('e0000000-0000-0000-0000-000000000004', 'Sesiones de 45 min en formación en valores.',
   'Ofrecer clases de formación en valores entre dos voluntarias. Sesiones de 45 minutos una vez por semana.', 'LIBRO', 0),
  ('e0000000-0000-0000-0000-000000000004', 'Sesiones de 50 min en apoyo académico.',
   'Ofrecer apoyo a alumnos con rezago escolar una vez por semana. Se les asigna un alumno fijo a quien apoyarían durante el semestre en lecto-escritura o matemáticas según sea necesario.', 'LIBRO', 1),
  ('e0000000-0000-0000-0000-000000000005', 'Armar kits de higiene personal para las mamás.',
   'Armar kits de higiene personal para las mamás con toallas húmedas para bebés, chanclas, pañales, shampoo, cepillo de dientes, papel sanitario y agua.', 'CORAZON', 0),
  ('e0000000-0000-0000-0000-000000000005', 'Entrega de desayunos a familiares de pacientes.',
   'Repartir aproximadamente 200 desayunos para los familiares de pacientes.', 'CORAZON', 1);

-- ─── Directorio de visiteo (familiasquesuman.com/directorio) ────────────────
INSERT INTO centros (id, nombre, tipo, resumen, informacion, necesidades, direccion, verificado, telefono, whatsapp, logo_clave,
                     como_ayudar, recomendaciones, instagram) VALUES
  ('f0000000-0000-0000-0000-000000000001', 'Morada del Anciano Desvalido Cadereyta', 'Asilos',
   'Asilo de ancianos donde se atienden 24 horas a 46 adultos mayores.',
   'Atención a adultos mayores en abandono, soledad y falta de apoyo familiar. Se les proporciona una vida digna. Se les ofrece refugio, alimentación, atención integral y compañía.',
   ARRAY['Alimentos como: azúcar, leche, aceite, té, gelatina, mole en lata, saladitas, servilletas, ensure, jugos y frutas',
         'Limpieza: trapeadores, cubetas, botes de basura, guantes, cloro, fabuloso, pino, jabón líquido, shampoo, desengrasantes, bolsas de basura'],
   'Blvd José María González #1000, Cadereyta', true, '8282844311', '3318290852', 'centro_amad',
   'Aportación en especie. Visita a los ancianos para convivir y platicar.',
   'Hablar con anticipación para ver qué actividades se recomiendan y en qué horario.', NULL),
  ('f0000000-0000-0000-0000-000000000002', 'Casa de la Misericordia', 'Casas hogar',
   'Casa Hogar que ofrece albergue, alimento y atención a jóvenes y adultos con enfermedades irreversibles.',
   'Hogar de la Misericordia ofrece vida digna a personas con enfermedades irreversibles y/o terminales no contagiosas, en completo desamparo y sin recursos económicos. Les brindan albergue, vestido, alimentación, medicamentos, terapias físicas, asistencia médica, dental y de enfermería.',
   ARRAY['Pañales de adulto tamaño mediano y grande',
         'Alimentos como aceite, atún en lata, azúcar, sal, galletas maría, arroz, pasta, frijoles, leche deslactosada y de almendra',
         'Productos de limpieza como fabuloso, bolsas de basura jumbo, jabón líquido para manos, jabón para trastes, pinol y papel sanitario'],
   'Monterrey', false, '8183368767', NULL, 'centro_misericordia',
   E'Visitas a los pacientes.\nAportación de alimentos, de productos de limpieza y de higiene personal.\nAportación de ropa de jóvenes y adultos.',
   E'Se aceptan visitas de niños y adultos con previo aviso y agenda.\nSe recomienda llevar alguna actividad planeada para convivir.\nNo más de 15 personas en cada visita.\nLos pacientes disfrutan de actividades con colores, burbujas y música.', NULL),
  ('f0000000-0000-0000-0000-000000000003', 'La Gran Familia', 'Casas hogar',
   'Casa Hogar de niños, niñas y adolescentes que fueron retirados de sus familias por falta de cuidados parentales ubicados en el Municipio de Santiago, NL.',
   'La Gran Familia es una casa hogar que atiende a niños, niñas y adolescentes con situación familiar vulnerable desde hace 44 años. Ofrecen vivienda, alimentación, educación, salud mental y general.',
   ARRAY['Visitas acompañadas de actividades de integración y socialización',
         'Alimentos, ropa, productos de limpieza e higiene personal, útiles escolares', 'Tenis blancos y zapato escolar'],
   'Villa de Santiago', true, NULL, '8126240294', 'centro_gran_familia',
   'Visita a los niños, niñas y adolescentes con actividades que generen convivencia y socialización. Se pueden organizar dinámicas, juegos, manualidades, lectura de cuentos o talleres que ayuden a crear momentos de alegría, aprendizaje y compañía.',
   E'Solo adultos para visitas internas.\nSe requiere organización previa con la institución para confirmar calendario.\nSe recomienda llevar una actividad para los niños y refrigerio para compartir.',
   'https://www.instagram.com/lagranfamiliaac'),
  ('f0000000-0000-0000-0000-000000000004', 'Comedor Apadrina un Niño', 'Comedores',
   'Comedor para apoyar a las familias de pacientes internados en la Clínica 25.',
   'Comedor que sirve aproximadamente 200 comidas de lunes a viernes de 1:00 a 3:00 pm.',
   ARRAY['Desechables', 'Alimentos no perecederos'],
   'Cuautla #208, Colonia 5 de Mayo, Monterrey', true, '8131117884', '8131117884', 'centro_apadrina',
   'Voluntarios bienvenidos a apoyar a servir comida todos los días de lunes a viernes. Puede ser por día.',
   E'Adultos y niños bienvenidos.\nHablar antes para confirmar asistencia.', 'https://www.instagram.com/apadrinaunnin/'),
  ('f0000000-0000-0000-0000-000000000005', 'Apadrina un niño', 'Casas hogar',
   'Albergue para niños con cáncer y comedor para sus familias.',
   'Apadrina un niño es un albergue donde pueden hospedarse niños que vengan a Monterrey a algún tratamiento médico. La casa recibe a 40 niños más un adulto que los acompañe. El albergue sirve alrededor de 200 comidas diarias de lunes a viernes a todos los familiares que tengan algún paciente internado y hospeda los días que sean necesarios sin costo alguno.',
   ARRAY['Alimentos no perecederos', 'Productos de higiene personal', 'Ropa de niño y adulto', 'Productos de limpieza',
         'Ropa de cama tamaño individual', 'Pañales, toallas húmedas para bebé', 'Juguetes', 'Mobiliario en general', 'Voluntarios para el comedor'],
   'Cuautla 208, Col. 5 de Mayo, 64186 Monterrey, N.L.', true, '8131117884', '8131117884', 'centro_apadrina',
   'Se buscan voluntarios de lunes a viernes para servir comida de 1:00 a 3:00. Puede asistir por día.',
   E'No es necesario llevar aportaciones para visitar.\nSe recomienda hablar antes de visitar para que sepan de su visita.',
   'https://www.instagram.com/apadrinaunnin/')
ON CONFLICT (id) DO UPDATE SET
  nombre = EXCLUDED.nombre, tipo = EXCLUDED.tipo, resumen = EXCLUDED.resumen, informacion = EXCLUDED.informacion,
  necesidades = EXCLUDED.necesidades, direccion = EXCLUDED.direccion, verificado = EXCLUDED.verificado,
  telefono = EXCLUDED.telefono, whatsapp = EXCLUDED.whatsapp, logo_clave = EXCLUDED.logo_clave,
  como_ayudar = EXCLUDED.como_ayudar, recomendaciones = EXCLUDED.recomendaciones, instagram = EXCLUDED.instagram;

-- Su lugar en el mapa (igual que la migración V10; solo si todavía no tienen uno).
UPDATE centros SET latitud = 25.724019, longitud = -100.344149
 WHERE id IN ('f0000000-0000-0000-0000-000000000004', 'f0000000-0000-0000-0000-000000000005') AND latitud IS NULL;
UPDATE centros SET latitud = 25.469243, longitud = -100.169873
 WHERE id = 'f0000000-0000-0000-0000-000000000003' AND latitud IS NULL;
UPDATE centros SET latitud = 25.587699, longitud = -100.001246
 WHERE id = 'f0000000-0000-0000-0000-000000000001' AND latitud IS NULL;

-- ─── La familia de prueba (solo se agrega si no existe) ─────────────────────
INSERT INTO cuentas (id, correo, contrasena_hash, rol, permisos, nombre_familia, ciudad, titular_nombre, titular_nacimiento, titular_sexo, aviso_aceptado_en) VALUES
  ('c0000000-0000-0000-0000-000000000002', 'ana.rodriguez@correo.com',
   '$argon2id$v=19$m=19456,t=2,p=1$ll8cSI2byTIoj5Dh0EvKzg$6pWa/GriWggzfGf8mgugOKaPghGhEFxtqos1IwfjffU', 'familia', '{}', 'Familia Rodríguez', 'Monterrey',
   'Ana Rodríguez', '1988-05-20', 'FEMENINO', now()),
  ('c0000000-0000-0000-0000-000000000001', 'admin@correo.com',
   '$argon2id$v=19$m=19456,t=2,p=1$c4A4M38t8CDtAbRpANxWow$Woy/CA2GUQHEdGPF2h5Uhf2czIF90dRru1aPWibmIvc', 'admin',
   ARRAY['administrar:catalogo', 'moderar:contenido', 'registrar:asistencias', 'atender:privacidad'],
   'Administración Familias que Suman', 'Monterrey', 'Administración', NULL, NULL, now())
ON CONFLICT DO NOTHING;
-- Si la base ya tenía estas cuentas de antes (sin contraseña, de cuando entraban con Auth0), se
-- les pone la de prueba. Una contraseña que alguien ya cambió no se toca.
UPDATE cuentas SET contrasena_hash = '$argon2id$v=19$m=19456,t=2,p=1$ll8cSI2byTIoj5Dh0EvKzg$6pWa/GriWggzfGf8mgugOKaPghGhEFxtqos1IwfjffU'
 WHERE id = 'c0000000-0000-0000-0000-000000000002' AND contrasena_hash IS NULL;
UPDATE cuentas SET contrasena_hash = '$argon2id$v=19$m=19456,t=2,p=1$c4A4M38t8CDtAbRpANxWow$Woy/CA2GUQHEdGPF2h5Uhf2czIF90dRru1aPWibmIvc', rol = 'admin',
       permisos = ARRAY['administrar:catalogo', 'moderar:contenido', 'registrar:asistencias', 'atender:privacidad']
 WHERE id = 'c0000000-0000-0000-0000-000000000001' AND contrasena_hash IS NULL;

INSERT INTO preferencias (cuenta_id) VALUES ('c0000000-0000-0000-0000-000000000002'), ('c0000000-0000-0000-0000-000000000001') ON CONFLICT DO NOTHING;

INSERT INTO acompanantes (id, cuenta_id, nombre, fecha_nacimiento, sexo) VALUES
  ('a0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002', 'Mateo Rodríguez', '2017-03-14', 'MASCULINO'),
  ('a0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 'Renata Rodríguez', '2019-08-02', 'FEMENINO')
ON CONFLICT DO NOTHING;

INSERT INTO favoritos (cuenta_id, tipo, objeto_id) VALUES
  ('c0000000-0000-0000-0000-000000000002', 'asociacion', 'b0000000-0000-0000-0000-000000000011'),
  ('c0000000-0000-0000-0000-000000000002', 'asociacion', 'b0000000-0000-0000-0000-000000000012'),
  ('c0000000-0000-0000-0000-000000000002', 'asociacion', 'b0000000-0000-0000-0000-000000000013')
ON CONFLICT DO NOTHING;

-- Inscrita a la Posada Sendero (ella sola), para poder ver Mis Actividades y cancelar.
INSERT INTO inscripciones (id, cuenta_id, actividad_id, personas) VALUES
  ('90000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000002', 1)
ON CONFLICT DO NOTHING;

-- Su historial: cuatro actividades que ya pasaron (las registró la asociación).
INSERT INTO asistencias (id, cuenta_id, actividad_id) VALUES
  ('91000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000003'),
  ('91000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000007'),
  ('91000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000006'),
  ('91000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000005')
ON CONFLICT DO NOTHING;

-- Ya revisados y aprobados (RF-12): un testimonio nuevo, en cambio, nace "en revisión".
INSERT INTO testimonios (id, asistencia_id, experiencia, foto_clave, estado, revisado_en) VALUES
  ('92000000-0000-0000-0000-000000000011', '91000000-0000-0000-0000-000000000003',
   'Mis hijos prepararon dibujos para los niños del hospital y volvieron con ganas de ayudar más. Fue la mejor tarde en familia que hemos tenido.', NULL, 'aprobado', now()),
  ('92000000-0000-0000-0000-000000000012', '91000000-0000-0000-0000-000000000002',
   'Entregamos kits de higiene y los niños se dieron cuenta de lo mucho que pueden aportar.', NULL, 'aprobado', now())
ON CONFLICT DO NOTHING;
-- Un seed anterior dejó un testimonio con una foto de ejemplo que nunca existió en el almacén
-- (la app pedía una foto que da 404). En las bases que lo tengan, se queda sin foto.
UPDATE testimonios SET foto_clave = NULL WHERE foto_clave = 'testimonios/ejemplo-1.jpg';

INSERT INTO respuestas_encuesta (cuenta_id, actividad_id, asistencia_id, momento, respuestas) VALUES
  ('c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000006', '91000000-0000-0000-0000-000000000003', 'despues',
   '[{"numero":1,"pregunta":"¿Cómo se sintieron en la actividad?","respuesta":"Muy bien, volveríamos"},
     {"numero":2,"pregunta":"¿Qué aprendieron tus hijos en esta actividad?","respuesta":"Entendieron mejor cómo viven otras familias"},
     {"numero":3,"pregunta":"¿Qué tan clara fue la organización del día?","respuesta":"Muy clara"},
     {"numero":4,"pregunta":"¿Volverían a participar en otra actividad?","respuesta":"Sí, pronto"}]')
ON CONFLICT DO NOTHING;

INSERT INTO aportaciones (id, cuenta_id, articulo_id, cantidad) VALUES
  ('f3000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002', 'f2000000-0000-0000-0000-000000000001', 10)
ON CONFLICT DO NOTHING;

-- ─── Sugerencias de ejemplo (para que la bandeja del admin no arranque vacía) ───
INSERT INTO sugerencias (id, tipo, nombre, descripcion, detalle_extra, ciudad, organizacion, contacto_causa, sugerida_por, contacto, cuenta_id) VALUES
  ('f4000000-0000-0000-0000-000000000002', 'centro', 'Casa Hogar Nuevo Amanecer',
   'Casa hogar para niñas de 6 a 17 años. Reciben visitas de familias los fines de semana y siempre necesitan apoyo con tareas escolares.',
   'Av. Ruiz Cortines 2500, Guadalupe', 'Monterrey', 'Nuevo Amanecer A.B.P.', '8112345678', 'Familia Garza', 'garza.familia@correo.com', NULL),
  ('f4000000-0000-0000-0000-000000000001', 'actividad', 'Limpieza del río Santa Catarina',
   'Jornada de limpieza en familia con la asociación del barrio. Ponen guantes y bolsas; los niños pueden ayudar a separar reciclables.',
   'Último domingo del mes, 8:00 am, puente Gonzalitos', 'Monterrey', '', '', 'Ana Rodríguez', 'ana.rodriguez@correo.com',
   'c0000000-0000-0000-0000-000000000002')
ON CONFLICT (id) DO UPDATE SET detalle_extra = EXCLUDED.detalle_extra, organizacion = EXCLUDED.organizacion,
  contacto_causa = EXCLUDED.contacto_causa, descripcion = EXCLUDED.descripcion;
