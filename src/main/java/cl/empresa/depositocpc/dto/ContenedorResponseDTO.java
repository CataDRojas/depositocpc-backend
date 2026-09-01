package cl.empresa.depositocpc.dto;

import cl.empresa.depositocpc.enums.Condicion;
import cl.empresa.depositocpc.enums.EstadoContenedor;
import cl.empresa.depositocpc.enums.TamanoContenedor;
import cl.empresa.depositocpc.enums.TipoContenedor;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ContenedorResponseDTO(
        UUID id,
        String numeroContenedor,
        TipoContenedor tipo,
        TamanoContenedor tamano,
        Boolean reeferConectado,
        Condicion condicion,
        EstadoContenedor estado,
        int nivel,
        OffsetDateTime fechaIngreso,
        UbicacionDTO ubicacion,
        long diasEnDeposito,
        boolean enAlerta
) {
}
