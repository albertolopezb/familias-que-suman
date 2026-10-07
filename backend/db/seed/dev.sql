-- Datos de prueba SOLO para desarrollo (nunca en un servidor real).
-- Se pueden correr varias veces: no duplican nada.
--
--   docker compose --profile seed run --rm seed
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
