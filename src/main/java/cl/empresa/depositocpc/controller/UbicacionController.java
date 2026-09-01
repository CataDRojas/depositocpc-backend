package cl.empresa.depositocpc.controller;

import cl.empresa.depositocpc.dto.NivelActualDTO;
import cl.empresa.depositocpc.dto.SugerenciaReubicacionDTO;
import cl.empresa.depositocpc.dto.UbicacionDTO;
import cl.empresa.depositocpc.dto.UbicacionRequestDTO;
import cl.empresa.depositocpc.service.ContenedorService;
import cl.empresa.depositocpc.service.UbicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ubicaciones")
@RequiredArgsConstructor
public class UbicacionController {

    private final UbicacionService ubicacionService;
    private final ContenedorService contenedorService;

    @GetMapping
    public ResponseEntity<List<UbicacionDTO>> listar() {
        return ResponseEntity.ok(ubicacionService.listar());
    }

    @PostMapping
    public ResponseEntity<UbicacionDTO> crear(@Valid @RequestBody UbicacionRequestDTO peticion) {
        UbicacionDTO ubicacion = ubicacionService.crear(peticion);
        return ResponseEntity
                .created(URI.create("/api/ubicaciones/" + ubicacion.id()))
                .body(ubicacion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UbicacionDTO> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody UbicacionRequestDTO peticion) {
        return ResponseEntity.ok(ubicacionService.actualizar(id, peticion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        ubicacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bloques")
    public ResponseEntity<List<String>> listarBloques() {
        return ResponseEntity.ok(ubicacionService.listarBloques());
    }

    @GetMapping("/bloques/{bloque}/bahias")
    public ResponseEntity<List<String>> listarBahias(@PathVariable String bloque) {
        return ResponseEntity.ok(ubicacionService.listarBahias(bloque));
    }

    @GetMapping("/bloques/{bloque}/bahias/{bahia}/filas")
    public ResponseEntity<List<String>> listarFilas(
            @PathVariable String bloque,
            @PathVariable String bahia) {
        return ResponseEntity.ok(ubicacionService.listarFilas(bloque, bahia));
    }

    @GetMapping("/nivel-actual")
    public ResponseEntity<NivelActualDTO> obtenerNivelActual(
            @RequestParam String bloque,
            @RequestParam String bahia,
            @RequestParam String fila) {
        return ResponseEntity.ok(contenedorService.obtenerNivelActual(bloque, bahia, fila));
    }

    @GetMapping("/sugerencias-reubicacion")
    public ResponseEntity<List<SugerenciaReubicacionDTO>> sugerirReubicacion(
            @RequestParam String bloque,
            @RequestParam String bahia,
            @RequestParam String fila) {
        return ResponseEntity.ok(ubicacionService.sugerirReubicacion(bloque, bahia, fila));
    }
}
