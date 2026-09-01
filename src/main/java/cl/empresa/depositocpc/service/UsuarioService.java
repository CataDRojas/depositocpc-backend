package cl.empresa.depositocpc.service;

import cl.empresa.depositocpc.dto.CrearUsuarioRequestDTO;
import cl.empresa.depositocpc.dto.CrearUsuarioResponseDTO;
import cl.empresa.depositocpc.dto.UsuarioResponseDTO;
import cl.empresa.depositocpc.entity.Usuario;
import cl.empresa.depositocpc.exception.ConflictoException;
import cl.empresa.depositocpc.exception.RecursoNoEncontradoException;
import cl.empresa.depositocpc.repository.UsuarioRepository;
import cl.empresa.depositocpc.security.GeneradorPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeneradorPassword generadorPassword;

    @Transactional
    public CrearUsuarioResponseDTO crear(CrearUsuarioRequestDTO peticion) {
        if (usuarioRepository.existsByEmail(peticion.email())) {
            throw new ConflictoException("Ya existe un usuario con el email " + peticion.email());
        }

        String passwordPlano = generadorPassword.generar();
        String hash = passwordEncoder.encode(passwordPlano);

        Usuario usuario = new Usuario();
        usuario.setEmail(peticion.email());
        usuario.setPasswordHash(hash);
        usuario.setNombre(peticion.nombre());
        usuario.setApellido(peticion.apellido());
        usuario.setRol(peticion.rol());

        usuarioRepository.save(usuario);

        return new CrearUsuarioResponseDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol(),
                passwordPlano
        );
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream()
                .map(u -> new UsuarioResponseDTO(
                        u.getId(), u.getEmail(), u.getNombre(), u.getApellido(),
                        u.getRol(), u.getActivo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        return new UsuarioResponseDTO(
                usuario.getId(), usuario.getEmail(), usuario.getNombre(), usuario.getApellido(),
                usuario.getRol(), usuario.getActivo());
    }

    @Transactional
    public UsuarioResponseDTO actualizar(UUID id, CrearUsuarioRequestDTO peticion) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));

        usuario.setNombre(peticion.nombre());
        usuario.setApellido(peticion.apellido());
        usuario.setRol(peticion.rol());

        usuarioRepository.save(usuario);

        return new UsuarioResponseDTO(
                usuario.getId(), usuario.getEmail(), usuario.getNombre(), usuario.getApellido(),
                usuario.getRol(), usuario.getActivo());
    }

    @Transactional
    public UsuarioResponseDTO cambiarEstado(UUID id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));

        usuario.setActivo(activo);
        usuarioRepository.save(usuario);

        return new UsuarioResponseDTO(
                usuario.getId(), usuario.getEmail(), usuario.getNombre(), usuario.getApellido(),
                usuario.getRol(), usuario.getActivo());
    }
}
