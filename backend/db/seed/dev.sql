-- Datos de prueba SOLO para desarrollo. Nunca se corren en un servidor real.
-- Se puede correr cuantas veces se quiera: no duplica nada (ON CONFLICT DO NOTHING).
--
--   docker compose --profile seed run --rm seed
--
-- Cuentas de prueba (las mismas que usa la app mientras no hay API):
--   ana.rodriguez@correo.com / familia123   (familia)
--   admin@correo.com         / admin123     (admin)
--
-- Los uuid fijos de aquí son para que el seed sea repetible y se pueda hacer referencia a
-- ellos en pruebas; en producción los genera la base de datos.

BEGIN;

-- ── Cuentas ──
INSERT INTO cuentas (id, correo, contrasena_hash, rol, nombre_familia, ciudad) VALUES
  ('c0000000-0000-0000-0000-000000000001', 'admin@correo.com',
   crypt('admin123', gen_salt('bf')), 'admin', 'Administrador General', 'Monterrey'),
  ('c0000000-0000-0000-0000-000000000002', 'ana.rodriguez@correo.com',
   crypt('familia123', gen_salt('bf')), 'familia', 'Familia Rodríguez', 'Monterrey')
ON CONFLICT DO NOTHING;

INSERT INTO acompanantes (id, cuenta_id, nombre, edad, es_titular) VALUES
  ('a0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'Administrador General', 30, true),
  ('a0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 'Ana Rodríguez', 38, true),
  ('a0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000002', 'Mateo Rodríguez', 9, false),
  ('a0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000002', 'Sofía Rodríguez', 6, false)
ON CONFLICT DO NOTHING;

INSERT INTO preferencias (cuenta_id) VALUES
  ('c0000000-0000-0000-0000-000000000001'),
  ('c0000000-0000-0000-0000-000000000002')
ON CONFLICT DO NOTHING;

-- ── Catálogo ──
INSERT INTO asociaciones (id, nombre, categoria, descripcion, ciudad) VALUES
  ('b0000000-0000-0000-0000-000000000001', 'Regalando Estrellas', 'Infancia',
   'Llevamos regalos, juguetes y kits de higiene a niñas y niños en hospitales.', 'Monterrey'),
  ('b0000000-0000-0000-0000-000000000002', 'Cíclica', 'Medio ambiente',
   'Limpiezas comunitarias y cultura del reciclaje en familia.', 'Monterrey')
ON CONFLICT DO NOTHING;

INSERT INTO actividades (id, asociacion_id, titulo, descripcion, inicia_en, termina_en, direccion,
                         municipio, cupo_total, tema, aportacion_tipo, aportacion_detalle) VALUES
  -- Pasada: sirve para el historial, el testimonio y la encuesta de después.
  ('d0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001',
   'Regalando Estrellas Visita al Materno Infantil',
   'Entrega de aproximadamente 200 desayunos, regalos, juguetes y kits de higiene personal.',
   '2026-09-05 09:45-06', '2026-09-05 13:00-06', 'Av. San Rafael No. 450', 'Guadalupe', 5,
   'salud', 'en_especie', 'Un juguete nuevo o un kit de higiene'),
  -- Próxima, con cupo.
  ('d0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002',
   'Posada Sendero', 'Posada comunitaria con piñata, cena y actividades para las familias.',
   '2026-11-28 09:30-06', '2026-11-28 13:00-06', 'San Ildefonso SN, Colonia Sendero', 'Santa Catarina', 40,
   'celebracion', 'ninguna', NULL)
ON CONFLICT DO NOTHING;

INSERT INTO campanas (id, asociacion_id, titulo, categoria, urgente, descripcion, unidad_meta, meta_total) VALUES
  ('e0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001',
   'Suma a su Mesa', 'Alimentación', true,
   'Juntamos despensas para familias de la colonia.', 'despensas', 100)
ON CONFLICT DO NOTHING;

INSERT INTO articulos_meta (id, campana_id, nombre, meta) VALUES
  ('e1000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'Arroz 1 kg', 100),
  ('e1000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000001', 'Frijol 1 kg', 100)
ON CONFLICT DO NOTHING;

INSERT INTO puntos_entrega (id, campana_id, direccion, colonia) VALUES
  ('e2000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'Calle Cóndor 1001', 'Fraccionamiento Azhara')
ON CONFLICT DO NOTHING;

INSERT INTO proyectos (id, nombre, descripcion, beneficiarios, ciudad, activo) VALUES
  ('f0000000-0000-0000-0000-000000000001', 'Voluntariado DIF Te Acompaña',
   'Acompañar a adultos mayores o jóvenes vulnerables con visitas en familia.',
   '200 adultos mayores', 'San Pedro Garza García', true)
ON CONFLICT DO NOTHING;

INSERT INTO centros_visiteo (id, nombre, tipo, resumen, necesidades, direccion) VALUES
  ('f1000000-0000-0000-0000-000000000001', 'Comedor Apadrina un Niño', 'Comedores',
   'Comedor comunitario que recibe voluntarios entre semana.',
   ARRAY['Voluntarios de lunes a viernes', 'Alimentos no perecederos'],
   'Cuautla 208, Col. 5 de Mayo, Monterrey')
ON CONFLICT DO NOTHING;

INSERT INTO favoritos (cuenta_id, tipo, objeto_id) VALUES
  ('c0000000-0000-0000-0000-000000000002', 'asociacion', 'b0000000-0000-0000-0000-000000000001')
ON CONFLICT DO NOTHING;

-- ── Inscripciones, asistencias, testimonio y encuestas de la familia de prueba ──
INSERT INTO inscripciones (id, cuenta_id, actividad_id) VALUES
  ('90000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000002')
ON CONFLICT DO NOTHING;

INSERT INTO inscripcion_asistentes (inscripcion_id, acompanante_id) VALUES
  ('90000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002'),
  ('90000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003')
ON CONFLICT DO NOTHING;

INSERT INTO asistencias (id, cuenta_id, actividad_id) VALUES
  ('91000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001')
ON CONFLICT DO NOTHING;

-- La foto es solo una clave de ejemplo: el objeto no existe en el almacenamiento.
INSERT INTO testimonios (id, asistencia_id, experiencia, foto_clave) VALUES
  ('92000000-0000-0000-0000-000000000001', '91000000-0000-0000-0000-000000000001',
   'Mis hijos prepararon dibujos para los niños del hospital y volvieron con ganas de ayudar más.',
   'testimonios/seed-1.jpg')
ON CONFLICT DO NOTHING;

INSERT INTO respuestas_encuesta (cuenta_id, actividad_id, momento, respuestas) VALUES
  ('c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'despues',
   '[{"numero":1,"pregunta":"¿Cómo se sintieron en la actividad?","respuesta":"Muy bien, volveríamos"},
     {"numero":2,"pregunta":"¿Qué aprendieron tus hijos en esta actividad?","respuesta":"Entendieron mejor cómo viven otras familias"}]')
ON CONFLICT DO NOTHING;

INSERT INTO sugerencias (id, tipo, nombre, descripcion, detalle_extra, ciudad, sugerida_por, contacto_de_quien_sugiere, cuenta_id) VALUES
  ('93000000-0000-0000-0000-000000000001', 'actividad', 'Limpieza del río Santa Catarina',
   'Jornada de limpieza en familia con la asociación del barrio.', 'Último domingo del mes, 8:00 am',
   'Monterrey', 'Ana Rodríguez', 'ana.rodriguez@correo.com', 'c0000000-0000-0000-0000-000000000002')
ON CONFLICT DO NOTHING;

COMMIT;
