package cl.empresa.depositocpc.dto;

import java.util.List;

/**
 * Envoltorio estable para respuestas paginadas, independiente de la
 * serialización por defecto de Page de Spring Data.
 */
public record PaginaDTO<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        boolean ultima
) {
}
