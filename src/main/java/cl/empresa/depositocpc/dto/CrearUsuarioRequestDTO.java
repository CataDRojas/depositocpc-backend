package cl.empresa.depositocpc.dto;

import cl.empresa.depositocpc.enums.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearUsuarioRequestDTO(
        @NotBlank @Email String email,
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotNull RolUsuario rol
) {
}
