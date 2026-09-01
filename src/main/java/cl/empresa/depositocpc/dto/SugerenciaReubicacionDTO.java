package cl.empresa.depositocpc.dto;

import java.util.UUID;

public record SugerenciaReubicacionDTO(
        UUID ubicacionId,
        String bloque,
        String bahia,
        String fila,
        int capacidadRestante
) {
}
