package cl.empresa.depositocpc.controller;

import cl.empresa.depositocpc.dto.ContenedorActualizacionDTO;
import cl.empresa.depositocpc.dto.ContenedorRequestDTO;
import cl.empresa.depositocpc.dto.ContenedorResponseDTO;
import cl.empresa.depositocpc.dto.PaginaDTO;
import cl.empresa.depositocpc.enums.EstadoContenedor;
import cl.empresa.depositocpc.enums.TipoContenedor;
import cl.empresa.depositocpc.service.ContenedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contenedores")
@RequiredArgsConstructor
public class ContenedorController {

    private static final int TAMANO_MAXIMO_PAGINA = 100;

    private final ContenedorService contenedorService;

    @PostMapping
    public ResponseEntity<ContenedorResponseDTO> ingresar(@Valid @RequestBody ContenedorRequestDTO peticion) {
        ContenedorResponseDTO respuesta = contenedorService.ingresar(peticion);
        return ResponseEntity
                .created(URI.create("/api/contenedores/" + respuesta.id()))
                .body(respuesta);
    }

    /**
     * Lista contenedores con filtros opcionales (estado, tipo, ubicación) y paginación.
     */
    @GetMapping
    public ResponseEntity<PaginaDTO<ContenedorResponseDTO>> listar(
            @RequestParam(required = false) EstadoContenedor estado,
            @RequestParam(required = false) TipoContenedor tipo,
            @RequestParam(name = "ubicacionId", required = false) UUID ubicacionId,
            @RequestParam(required = false) String bloque,
            @RequestParam(required = false) String bahia,
            @RequestParam(required = false) String fila,
            @RequestParam(name = "fechaDesde", required = false) LocalDate fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) LocalDate fechaHasta,
            @RequestParam(name = "page", defaultValue = "0") int pagina,
            @RequestParam(name = "size", defaultValue = "20") int tamano) {

        var pageable = PageRequest.of(pagina, Math.min(tamano, TAMANO_MAXIMO_PAGINA),
                Sort.by(Sort.Direction.DESC, "fechaIngreso"));
        return ResponseEntity.ok(contenedorService.listar(estado, tipo, ubicacionId,
                bloque, bahia, fila, fechaDesde, fechaHasta, pageable));
    }

    /**
     * Contenedores EN_DEPOSITO con 5 o más días en el depósito.
     */
    @GetMapping("/alertas")
    public ResponseEntity<List<ContenedorResponseDTO>> listarAlertas() {
        return ResponseEntity.ok(contenedorService.listarAlertas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContenedorResponseDTO> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(contenedorService.obtenerPorId(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ContenedorResponseDTO> actualizar(
            @PathVariable UUID id,
            @RequestBody ContenedorActualizacionDTO peticion) {
        return ResponseEntity.ok(contenedorService.actualizar(id, peticion));
    }

    @PostMapping("/{id}/despacho")
    public ResponseEntity<ContenedorResponseDTO> despachar(@PathVariable UUID id) {
        return ResponseEntity.ok(contenedorService.despachar(id));
    }
}
