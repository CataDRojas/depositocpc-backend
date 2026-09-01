package cl.empresa.depositocpc.dto;

import java.util.UUID;

public record UbicacionDTO(
        UUID id,
        String bloque,
        String bahia,
        String fila
) {
}
