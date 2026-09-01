package cl.empresa.depositocpc.dto;

import jakarta.validation.constraints.NotBlank;

public record UbicacionRequestDTO(
        @NotBlank(message = "El bloque es obligatorio")
        String bloque,

        @NotBlank(message = "La bahía es obligatoria")
        String bahia,

        @NotBlank(message = "La fila es obligatoria")
        String fila
) {
}
