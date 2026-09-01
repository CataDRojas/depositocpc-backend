package cl.empresa.depositocpc.controller;

import cl.empresa.depositocpc.dto.CrearUsuarioRequestDTO;
import cl.empresa.depositocpc.dto.CrearUsuarioResponseDTO;
import cl.empresa.depositocpc.dto.UsuarioResponseDTO;
import cl.empresa.depositocpc.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrearUsuarioResponseDTO> crear(@Valid @RequestBody CrearUsuarioRequestDTO peticion) {
        CrearUsuarioResponseDTO respuesta = usuarioService.crear(peticion);
        return ResponseEntity
                .created(URI.create("/api/usuarios/" + respuesta.id()))
                .body(respuesta);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @PathVariable UUID id,
            @RequestBody CrearUsuarioRequestDTO peticion) {
        return ResponseEntity.ok(usuarioService.actualizar(id, peticion));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstado(
            @PathVariable UUID id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, activo));
    }
}
