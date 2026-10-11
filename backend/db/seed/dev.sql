-- Datos de prueba SOLO para desarrollo (nunca en un servidor real).
-- Se pueden correr varias veces: no duplican nada.
--
--   Se cargan solos con `docker compose up -d`.
--
-- Cuentas de prueba:
--   ana.rodriguez@correo.com / familia123   (familia)
--   admin@correo.com         / admin123     (admin)

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- para cifrar las contraseñas de prueba con crypt()

INSERT INTO cuentas (id, correo, contrasena_hash, rol, nombre_familia) VALUES
  ('c0000000-0000-0000-0000-000000000001', 'admin@correo.com',
   crypt('admin123', gen_salt('bf')), 'admin', 'Administrador General'),
  ('c0000000-0000-0000-0000-000000000002', 'ana.rodriguez@correo.com',
   crypt('familia123', gen_salt('bf')), 'familia', 'Familia Rodríguez')
ON CONFLICT DO NOTHING;

INSERT INTO asociaciones (id, nombre, categoria, descripcion) VALUES
  ('b0000000-0000-0000-0000-000000000001', 'Regalando Estrellas', 'Infancia',
   'Llevamos regalos y kits de higiene a niñas y niños en hospitales.'),
  ('b0000000-0000-0000-0000-000000000002', 'Cíclica', 'Medio ambiente',
   'Limpiezas comunitarias y cultura del reciclaje en familia.')
ON CONFLICT DO NOTHING;

INSERT INTO actividades (id, asociacion_id, titulo, descripcion, inicia_en, direccion, cupo_total) VALUES
  ('d0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001',
   'Regalando Estrellas Visita al Materno Infantil', 'Entrega de regalos y kits de higiene.',
   '2026-11-07 09:45-06', 'Av. San Rafael No. 450', 5),
  ('d0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002',
   'Posada Sendero', 'Posada comunitaria con piñata, cena y actividades para las familias.',
   '2026-11-28 09:30-06', 'San Ildefonso SN, Colonia Sendero', 40)
ON CONFLICT DO NOTHING;

-- Ana y su familia (3 personas) ya están inscritas a la posada.
INSERT INTO inscripciones (id, cuenta_id, actividad_id, personas) VALUES
  ('90000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002',
   'd0000000-0000-0000-0000-000000000002', 3)
ON CONFLICT DO NOTHING;

-- ── V2 · Lo de la familia ──
INSERT INTO acompanantes (id, cuenta_id, nombre, fecha_nacimiento) VALUES
  ('a0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002', 'Mateo Rodríguez', '2017-03-14'),
  ('a0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 'Renata Rodríguez', '2019-08-02')
ON CONFLICT DO NOTHING;

INSERT INTO favoritos (cuenta_id, tipo, objeto_id) VALUES
  ('c0000000-0000-0000-0000-000000000002', 'asociacion', 'b0000000-0000-0000-0000-000000000001')
ON CONFLICT DO NOTHING;

INSERT INTO preferencias (cuenta_id) VALUES
  ('c0000000-0000-0000-0000-000000000001'),
  ('c0000000-0000-0000-0000-000000000002')
ON CONFLICT DO NOTHING;

-- ── V3 · Participación: una actividad que ya pasó, con su testimonio y su encuesta ──
INSERT INTO actividades (id, asociacion_id, titulo, descripcion, inicia_en, direccion, cupo_total) VALUES
  ('d0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002',
   'Mega Limpieza San Pedro', 'Actividad familiar para ayudar haciendo limpieza al medio ambiente.',
   '2026-09-26 08:00-06', 'Humberto Lobo esq. con Av. Ignacio Morones Prieto', 0)
ON CONFLICT DO NOTHING;

INSERT INTO asistencias (id, cuenta_id, actividad_id) VALUES
  ('91000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000003')
ON CONFLICT DO NOTHING;

-- La foto es solo una clave de ejemplo: el archivo no existe en el almacenamiento.
INSERT INTO testimonios (id, asistencia_id, experiencia, foto_clave) VALUES
  ('92000000-0000-0000-0000-000000000001', '91000000-0000-0000-0000-000000000001',
   'Limpiamos el parque en familia y los niños quieren volver el próximo mes.', 'testimonios/ejemplo-1.jpg')
ON CONFLICT DO NOTHING;

INSERT INTO respuestas_encuesta (cuenta_id, actividad_id, momento, respuestas) VALUES
  ('c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000003', 'despues',
   '[{"numero":1,"pregunta":"¿Cómo se sintieron en la actividad?","respuesta":"Muy bien, volveríamos"}]')
ON CONFLICT DO NOTHING;

INSERT INTO invitaciones (cuenta_id, actividad_id)
SELECT 'c0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000002'
WHERE NOT EXISTS (SELECT 1 FROM invitaciones);

-- ── V4 · Proyectos, Directorio y Donar ──
INSERT INTO proyectos (id, nombre, resumen, beneficiarios, ciudad, whatsapp) VALUES
  ('e0000000-0000-0000-0000-000000000001', 'Trazo... Escribiendo una nueva historia',
   'Voluntariado que visita a mujeres en el penal de Escobedo.', '25 Mujeres', 'Monterrey', '8110771068')
ON CONFLICT DO NOTHING;

INSERT INTO formas_apoyo (id, proyecto_id, titulo, detalle) VALUES
  ('e1000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001',
   'Compartir tu tiempo y talento', 'Pláticas de desarrollo humano y talleres de acuarela.'),
  ('e1000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000001',
   'Aportación económica', 'Para pinturas, pinceles, papel y alimentos de cada visita.')
ON CONFLICT DO NOTHING;

INSERT INTO centros (id, nombre, tipo, resumen, necesidades, direccion, verificado, telefono) VALUES
  ('f0000000-0000-0000-0000-000000000001', 'Morada del Anciano Desvalido Cadereyta', 'Asilos',
   'Asilo de ancianos donde se atienden 24 horas a 46 adultos mayores.',
   ARRAY['Alimentos', 'Limpieza'], 'Blvd José María González #1000, Cadereyta', true, '8282844311')
ON CONFLICT DO NOTHING;

INSERT INTO campanas (id, asociacion_id, titulo, descripcion, categoria) VALUES
  ('f1000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001',
   'Bibliotecas Infantiles', 'Recolección de cuentos infantiles para crear bibliotecas.', 'Útiles escolares')
ON CONFLICT DO NOTHING;

INSERT INTO articulos_meta (id, campana_id, nombre, meta) VALUES
  ('f2000000-0000-0000-0000-000000000001', 'f1000000-0000-0000-0000-000000000001', 'Cuentos infantiles', 300)
ON CONFLICT DO NOTHING;

INSERT INTO aportaciones (id, cuenta_id, articulo_id, cantidad) VALUES
  ('f3000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002',
   'f2000000-0000-0000-0000-000000000001', 10)
ON CONFLICT DO NOTHING;

-- ── V5 · Sugerencias ──
INSERT INTO sugerencias (id, tipo, nombre, descripcion, sugerida_por, contacto, cuenta_id) VALUES
  ('f4000000-0000-0000-0000-000000000001', 'actividad', 'Limpieza del río Santa Catarina',
   'Jornada de limpieza en familia con la asociación del barrio.', 'Ana Rodríguez',
   'ana.rodriguez@correo.com', 'c0000000-0000-0000-0000-000000000002')
ON CONFLICT DO NOTHING;
