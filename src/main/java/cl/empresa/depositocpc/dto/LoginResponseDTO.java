package cl.empresa.depositocpc.dto;

import cl.empresa.depositocpc.enums.RolUsuario;

public record LoginResponseDTO(
        String accessToken,
        String tokenType,
        String email,
        RolUsuario rol
) {
}
