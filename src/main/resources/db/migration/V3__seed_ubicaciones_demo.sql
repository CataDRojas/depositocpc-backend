-- Ubicaciones de demostración para desarrollo local.
-- Permite probar el ingreso de contenedores sin cargar el catálogo a mano.
INSERT INTO ubicacion (id, bloque, fila, nivel)
VALUES (gen_random_uuid(), 'A', '01', '1'),
       (gen_random_uuid(), 'A', '01', '2'),
       (gen_random_uuid(), 'A', '02', '1'),
       (gen_random_uuid(), 'B', '01', '1'),
       (gen_random_uuid(), 'B', '02', '2'),
       (gen_random_uuid(), 'C', '03', '3');
