package cl.empresa.depositocpc.dto;

import cl.empresa.depositocpc.enums.RolUsuario;

import java.util.UUID;

/**
 * Datos del usuario autenticado (endpoint /api/auth/me).
 */
public record UsuarioResponseDTO(
        UUID id,
        String email,
        RolUsuario rol
) {
}
