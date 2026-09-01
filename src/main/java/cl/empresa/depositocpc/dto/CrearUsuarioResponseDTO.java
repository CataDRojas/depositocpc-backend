package cl.empresa.depositocpc.dto;

import cl.empresa.depositocpc.enums.RolUsuario;

import java.util.UUID;

/**
 * Respuesta al crear un usuario. Inuye la contraseña generada una sola vez.
 */
public record CrearUsuarioResponseDTO(
        UUID id,
        String email,
        String nombre,
        String apellido,
        RolUsuario rol,
        String passwordGenerada
) {
}
