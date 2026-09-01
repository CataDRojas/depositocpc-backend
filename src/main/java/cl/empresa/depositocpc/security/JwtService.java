package cl.empresa.depositocpc.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Generación y validación de tokens JWT (HS256).
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration expiracion;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiracion-minutos}") long expiracionMinutos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracion = Duration.ofMinutes(expiracionMinutos);
    }

    public String generarToken(UserDetails usuarioDetails) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuarioDetails.getUsername())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracion)))
                .signWith(clave)
                .compact();
    }

    public String extraerEmail(String token) {
        return obtenerClaims(token).getSubject();
    }

    /**
     * Un token es válido si la firma es correcta y no está expirado.
     */
    public boolean esTokenValido(String token) {
        try {
            obtenerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
