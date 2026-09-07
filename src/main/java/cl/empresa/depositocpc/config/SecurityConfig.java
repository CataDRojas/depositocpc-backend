package cl.empresa.depositocpc.config;

import cl.empresa.depositocpc.security.JwtAuthFilter;
import cl.empresa.depositocpc.security.UsuarioDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración de seguridad del MVP:
 * - Autenticación obligatoria con JWT (sin sesión).
 * - Solo /api/auth/login y Swagger son públicos.
 * - /api/usuarios/** exige rol ADMIN (vía @PreAuthorize).
 * - Los orígenes CORS se inyectan vía propiedad (variable CORS_ORIGINS en
 *   producción, con fallback a localhost para desarrollo local).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UsuarioDetailsService usuarioDetailsService;
    private final List<String> origenesPermitidos;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          UsuarioDetailsService usuarioDetailsService,
                          @Value("${cors.origenes-permitidos}") List<String> origenesPermitidos) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.usuarioDetailsService = usuarioDetailsService;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public SecurityFilterChain cadenaFiltros(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(fuenteCors()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                // Sin token válido responde 401 (no 403) para indicar claramente que falta autenticarse
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((peticion, respuesta, errorAuth) -> {
                            respuesta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            respuesta.setContentType("application/json;charset=UTF-8");
                            respuesta.getWriter().write(
                                    "{\"codigo\":\"NO_AUTENTICADO\",\"mensaje\":\"Se requiere autenticación para acceder a este recurso\",\"campo\":null}");
                        })
                        .accessDeniedHandler((peticion, respuesta, accesoDenegado) -> {
                            respuesta.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            respuesta.setContentType("application/json;charset=UTF-8");
                            respuesta.getWriter().write(
                                    "{\"codigo\":\"ACCESO_DENEGADO\",\"mensaje\":\"No tienes permisos para acceder a este recurso\",\"campo\":null}");
                        }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource fuenteCors() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(origenesPermitidos);
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider proveedorAutenticacion(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider();
        proveedor.setUserDetailsService(usuarioDetailsService);
        proveedor.setPasswordEncoder(passwordEncoder);
        return proveedor;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider proveedorAutenticacion) {
        return new ProviderManager(proveedorAutenticacion);
    }
}
