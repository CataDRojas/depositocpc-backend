package cl.empresa.depositocpc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Utilidad para generar el hash BCrypt del usuario inicial (migración V2).
 *
 * <p>Uso: definir la variable de entorno PASSWORD_SEED y ejecutar:
 * <pre>
 *   PASSWORD_SEED='MiClaveSegura' mvn test -Dtest=GeneradorHashBcryptTest
 * </pre>
 * La contraseña nunca se escribe en el código fuente ni queda en el repo;
 * el hash resultante se copia a V2__seed_usuario_inicial.sql.
 */
class GeneradorHashBcryptTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "PASSWORD_SEED", matches = ".+")
    void generarHashBcrypt() {
        String contrasena = System.getenv("PASSWORD_SEED");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String hash = encoder.encode(contrasena);

        System.out.println("HASH_BCRYPT=" + hash);
        assertTrue(encoder.matches(contrasena, hash), "El hash generado debe validar la contraseña");
    }
}
