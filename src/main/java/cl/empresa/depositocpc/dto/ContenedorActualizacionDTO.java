package cl.empresa.depositocpc.dto;

import cl.empresa.depositocpc.enums.Condicion;
import cl.empresa.depositocpc.enums.TamanoContenedor;
import cl.empresa.depositocpc.enums.TipoContenedor;

import java.util.UUID;

public record ContenedorActualizacionDTO(
        TipoContenedor tipo,
        TamanoContenedor tamano,
        Boolean reeferConectado,
        Condicion condicion,
        UUID ubicacionId
) {
}
