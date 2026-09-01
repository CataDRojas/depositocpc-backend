-- Fase 2: Cambios de modelo para gravedad, LIFO y nueva estructura de ubicación.
--
-- Ubicacion: se reemplaza "nivel" por "bahía" (bloque + bahía + fila).
-- Contenedor: se agregan "nivel" (auto-calculado) y "tamano" (20/40/45).
-- Se limpian datos demo de V3 para re-crear con el nuevo formato.

-- Limpiar datos dependientes (solo datos demo de desarrollo)
DELETE FROM movimiento;
DELETE FROM contenedor;
DELETE FROM ubicacion;

-- Ubicacion: eliminar nivel, agregar bahía
ALTER TABLE ubicacion DROP CONSTRAINT uq_ubicacion;
ALTER TABLE ubicacion DROP COLUMN nivel;
ALTER TABLE ubicacion ADD COLUMN bahia VARCHAR(10) NOT NULL DEFAULT '01';
ALTER TABLE ubicacion ADD CONSTRAINT uq_ubicacion UNIQUE (bloque, bahia, fila);

-- Contenedor: agregar nivel (auto-calculado) y tamano
ALTER TABLE contenedor ADD COLUMN nivel INTEGER NOT NULL DEFAULT 1;
ALTER TABLE contenedor ADD COLUMN tamano VARCHAR(20) NOT NULL DEFAULT 'VEINTE';
ALTER TABLE contenedor ADD CONSTRAINT ck_contenedor_tamano
    CHECK (tamano IN ('VEINTE', 'CUARENTA', 'CUARENTA_CINCO'));

-- Limpiar el default de nivel (cada ingreso futuro lo calculará el sistema)
ALTER TABLE contenedor ALTER COLUMN nivel DROP DEFAULT;
