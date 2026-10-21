-- Los centros del Directorio (asilos, casas hogar, comedores) también salen en el mapa.
-- Igual que actividades, proyectos y campañas: latitud y longitud opcionales; sin ellas, el
-- centro no aparece en el mapa pero sigue en el Directorio.
ALTER TABLE centros
    ADD COLUMN latitud  double precision CHECK (latitud BETWEEN -90 AND 90),
    ADD COLUMN longitud double precision CHECK (longitud BETWEEN -180 AND 180);

-- Ubicaciones de los centros que ya existen, buscadas en OpenStreetMap con su dirección pública
-- (20 de octubre de 2026). Solo se ponen si el centro existe y no tiene ubicación todavía.
-- · Cuautla 208, Monterrey (Apadrina un niño y su comedor): la calle, aproximada.
-- · La Gran Familia, Santiago: el punto exacto de OpenStreetMap.
-- · Morada del Anciano Desvalido: el centro de Cadereyta, aproximado (la dirección no está en el mapa).
-- · Casa de la Misericordia: solo dice "Monterrey"; se queda sin ubicación hasta que la
--   administración la ponga desde el formulario.
UPDATE centros SET latitud = 25.724019, longitud = -100.344149
 WHERE id IN ('f0000000-0000-0000-0000-000000000004', 'f0000000-0000-0000-0000-000000000005') AND latitud IS NULL;
UPDATE centros SET latitud = 25.469243, longitud = -100.169873
 WHERE id = 'f0000000-0000-0000-0000-000000000003' AND latitud IS NULL;
UPDATE centros SET latitud = 25.587699, longitud = -100.001246
 WHERE id = 'f0000000-0000-0000-0000-000000000001' AND latitud IS NULL;
