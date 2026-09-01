package cl.empresa.depositocpc.service;

import cl.empresa.depositocpc.dto.LoginRequestDTO;
import cl.empresa.depositocpc.dto.LoginResponseDTO;
import cl.empresa.depositocpc.dto.UsuarioResponseDTO;
import cl.empresa.depositocpc.entity.Usuario;
import cl.empresa.depositocpc.exception.RecursoNoEncontradoException;
import cl.empresa.depositocpc.repository.UsuarioRepository;
import cl.empresa.depositocpc.security.JwtService;
import cl.empresa.depositocpc.security.UsuarioDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtService jwtService;

    public LoginResponseDTO iniciarSesion(LoginRequestDTO peticion) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(peticion.email(), peticion.password()));

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(peticion.email())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", peticion.email()));
        UserDetails usuarioDetails = usuarioDetailsService.loadUserByUsername(usuario.getEmail());

        String token = jwtService.generarToken(usuarioDetails);
        return new LoginResponseDTO(token, "Bearer", usuario.getEmail(), usuario.getRol());
    }

    public UsuarioResponseDTO obtenerPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", email));
        return new UsuarioResponseDTO(usuario.getId(), usuario.getEmail(), usuario.getRol());
    }
}
