package cl.empresa.depositocpc.service;

import cl.empresa.depositocpc.dto.SugerenciaReubicacionDTO;
import cl.empresa.depositocpc.dto.UbicacionDTO;
import cl.empresa.depositocpc.dto.UbicacionRequestDTO;
import cl.empresa.depositocpc.entity.Ubicacion;
import cl.empresa.depositocpc.exception.ConflictoException;
import cl.empresa.depositocpc.exception.PeticionInvalidaException;
import cl.empresa.depositocpc.exception.RecursoNoEncontradoException;
import cl.empresa.depositocpc.mapper.ContenedorMapper;
import cl.empresa.depositocpc.repository.ContenedorRepository;
import cl.empresa.depositocpc.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final ContenedorRepository contenedorRepository;
    private final ContenedorMapper contenedorMapper;

    @Value("${patio.altura-maxima-apilamiento:3}")
    private int alturaMaxima;

    @Transactional(readOnly = true)
    public List<UbicacionDTO> listar() {
        Sort orden = Sort.by(
                Sort.Order.asc("bloque"),
                Sort.Order.asc("bahia"),
                Sort.Order.asc("fila"));
        return ubicacionRepository.findAll(orden).stream()
                .map(contenedorMapper::toDto)
                .toList();
    }

    @Transactional
    public UbicacionDTO crear(UbicacionRequestDTO peticion) {
        String bloque = peticion.bloque().trim().toUpperCase();
        String bahia = peticion.bahia().trim().toUpperCase();
        String fila = peticion.fila().trim();

        if (ubicacionRepository.existsByBloqueAndBahiaAndFila(bloque, bahia, fila)) {
            throw new ConflictoException(
                    "Ya existe la posición " + bloque + "-" + bahia + "-" + fila);
        }

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setBloque(bloque);
        ubicacion.setBahia(bahia);
        ubicacion.setFila(fila);
        ubicacion = ubicacionRepository.save(ubicacion);
        return contenedorMapper.toDto(ubicacion);
    }

    @Transactional
    public UbicacionDTO actualizar(UUID id, UbicacionRequestDTO peticion) {
        Ubicacion ubicacion = ubicacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ubicación", id));

        String bloque = peticion.bloque().trim().toUpperCase();
        String bahia = peticion.bahia().trim().toUpperCase();
        String fila = peticion.fila().trim();

        boolean cambioPosicion = !ubicacion.getBloque().equals(bloque)
                || !ubicacion.getBahia().equals(bahia)
                || !ubicacion.getFila().equals(fila);

        if (cambioPosicion && ubicacionRepository.existsByBloqueAndBahiaAndFila(bloque, bahia, fila)) {
            throw new ConflictoException(
                    "Ya existe la posición " + bloque + "-" + bahia + "-" + fila);
        }

        ubicacion.setBloque(bloque);
        ubicacion.setBahia(bahia);
        ubicacion.setFila(fila);
        ubicacion = ubicacionRepository.save(ubicacion);
        return contenedorMapper.toDto(ubicacion);
    }

    @Transactional
    public void eliminar(UUID id) {
        Ubicacion ubicacion = ubicacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ubicación", id));

        long contenedores = contenedorRepository.contarEnPosicion(
                ubicacion.getBloque(), ubicacion.getBahia(), ubicacion.getFila());

        if (contenedores > 0) {
            throw new PeticionInvalidaException(
                    "No se puede eliminar la posición " + ubicacion.getBloque() + "-"
                            + ubicacion.getBahia() + "-" + ubicacion.getFila()
                            + " porque tiene " + contenedores + " contenedor(es) apilados",
                    "ubicacionId");
        }

        ubicacionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<String> listarBloques() {
        return ubicacionRepository.findBloquesUnicos();
    }

    @Transactional(readOnly = true)
    public List<String> listarBahias(String bloque) {
        return ubicacionRepository.findBahiasPorBloque(bloque);
    }

    @Transactional(readOnly = true)
    public List<String> listarFilas(String bloque, String bahia) {
        return ubicacionRepository.findFilasPorBloqueYBahia(bloque, bahia);
    }

    /**
     * Sugiere ubicaciones temporales disponibles para reubicar contenedores
     * que bloquean el despacho (LIFO). Retorna posiciones con espacio libre.
     */
    @Transactional(readOnly = true)
    public List<SugerenciaReubicacionDTO> sugerirReubicacion(String bloque, String bahia, String fila) {
        return ubicacionRepository.findAll(Sort.by(
                        Sort.Order.asc("bloque"),
                        Sort.Order.asc("bahia"),
                        Sort.Order.asc("fila")))
                .stream()
                .filter(u -> {
                    // Excluir la posición origen
                    if (u.getBloque().equals(bloque) && u.getBahia().equals(bahia) && u.getFila().equals(fila)) {
                        return false;
                    }
                    // Solo posiciones con espacio disponible
                    int nivelMaximo = contenedorRepository.nivelMaximoEnPosicion(
                            u.getBloque(), u.getBahia(), u.getFila());
                    return nivelMaximo < alturaMaxima;
                })
                .map(u -> {
                    int nivelMaximo = contenedorRepository.nivelMaximoEnPosicion(
                            u.getBloque(), u.getBahia(), u.getFila());
                    return new SugerenciaReubicacionDTO(
                            u.getId(),
                            u.getBloque(),
                            u.getBahia(),
                            u.getFila(),
                            alturaMaxima - nivelMaximo);
                })
                .toList();
    }
}
