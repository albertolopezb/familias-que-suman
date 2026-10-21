-- "Causas destacadas" del Inicio: las elige la administración con el interruptor "Destacar en el
-- Inicio" del formulario de la asociación. Si ninguna está destacada, la app enseña las dos primeras.
ALTER TABLE asociaciones ADD COLUMN destacada boolean NOT NULL DEFAULT false;
