-- Ubicaciones de demostración con el nuevo formato: bloque + bahía + fila.
-- Cada registro es una "base física" donde se pueden apilar contenedores.
INSERT INTO ubicacion (id, bloque, bahia, fila)
VALUES
    -- Bloque A: 3 bahías, 2 filas cada una
    (gen_random_uuid(), 'A', '01', '1'),
    (gen_random_uuid(), 'A', '01', '2'),
    (gen_random_uuid(), 'A', '02', '1'),
    (gen_random_uuid(), 'A', '02', '2'),
    (gen_random_uuid(), 'A', '03', '1'),
    -- Bloque B: 2 bahías, 2 filas cada una
    (gen_random_uuid(), 'B', '01', '1'),
    (gen_random_uuid(), 'B', '01', '2'),
    (gen_random_uuid(), 'B', '02', '1'),
    -- Bloque C: 1 bahía, 3 filas
    (gen_random_uuid(), 'C', '01', '1'),
    (gen_random_uuid(), 'C', '01', '2'),
    (gen_random_uuid(), 'C', '01', '3');
