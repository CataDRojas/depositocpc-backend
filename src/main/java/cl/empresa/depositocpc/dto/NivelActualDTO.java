package cl.empresa.depositocpc.dto;

public record NivelActualDTO(
        String bloque,
        String bahia,
        String fila,
        int nivelActual,
        int siguienteNivel,
        boolean llena
) {
}
