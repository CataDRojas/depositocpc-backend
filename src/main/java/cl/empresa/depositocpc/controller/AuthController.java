package cl.empresa.depositocpc.controller;

import cl.empresa.depositocpc.dto.LoginRequestDTO;
import cl.empresa.depositocpc.dto.LoginResponseDTO;
import cl.empresa.depositocpc.dto.UsuarioResponseDTO;
import cl.empresa.depositocpc.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO peticion) {
        return ResponseEntity.ok(authService.iniciarSesion(peticion));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> perfilActual(Authentication autenticacion) {
        return ResponseEntity.ok(authService.obtenerPerfil(autenticacion.getName()));
    }
}
