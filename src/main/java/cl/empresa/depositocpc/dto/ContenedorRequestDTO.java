package cl.empresa.depositocpc.dto;

import cl.empresa.depositocpc.enums.Condicion;
import cl.empresa.depositocpc.enums.TamanoContenedor;
import cl.empresa.depositocpc.enums.TipoContenedor;
import cl.empresa.depositocpc.validation.NumeroContenedorValido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ContenedorRequestDTO(

        @NotBlank(message = "El número de contenedor es obligatorio")
        @NumeroContenedorValido
        String numeroContenedor,

        @NotNull(message = "El tipo de contenedor es obligatorio")
        TipoContenedor tipo,

        @NotNull(message = "El tamaño del contenedor es obligatorio")
        TamanoContenedor tamano,

        Boolean reeferConectado,

        @NotNull(message = "La condición del contenedor es obligatoria")
        Condicion condicion,

        @NotNull(message = "La ubicación es obligatoria")
        UUID ubicacionId,

        OffsetDateTime fechaIngreso
) {
}
