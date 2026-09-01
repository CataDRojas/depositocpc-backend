package cl.empresa.depositocpc.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Genera contraseñas aleatorias seguras para nuevos usuarios.
 * La contraseña se devuelve una sola vez al_ADMIN al crear el usuario.
 */
@Component
public class GeneradorPassword {

    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    private static final int LONGITUD = 12;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    public String generar() {
        StringBuilder password = new StringBuilder(LONGITUD);
        for (int i = 0; i < LONGITUD; i++) {
            password.append(CARACTERES.charAt(ALEATORIO.nextInt(CARACTERES.length())));
        }
        return password.toString();
    }
}
