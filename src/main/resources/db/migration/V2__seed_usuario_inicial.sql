-- Usuario inicial para desarrollo local (MVP sin pantalla de registro).
-- La contraseña NO está en el repo: solo su hash BCrypt.
-- Contraseña de desarrollo: Admin123!  (cambiar tras el primer uso; es solo local).
-- Para generar un hash nuevo ejecutar:
--   mvn test -Dtest=GeneradorHashBcryptTest
INSERT INTO usuario (id, email, password_hash, nombre, apellido, rol, activo, creado_en)
VALUES (
    gen_random_uuid(),
    'admin@cpc.cl',
    '$2a$10$oj.7SUL3BVzeRThgvAVgiu2UNiOfBl6VCJNiUQWgBeKyStj8VChUu',
    'Jorge',
    'Rojas',
    'ADMIN',
    true,
    now()
);
