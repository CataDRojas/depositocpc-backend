package cl.empresa.depositocpc.service;

import cl.empresa.depositocpc.dto.ContenedorActualizacionDTO;
import cl.empresa.depositocpc.dto.ContenedorRequestDTO;
import cl.empresa.depositocpc.dto.ContenedorResponseDTO;
import cl.empresa.depositocpc.dto.NivelActualDTO;
import cl.empresa.depositocpc.dto.PaginaDTO;
import cl.empresa.depositocpc.entity.Contenedor;
import cl.empresa.depositocpc.entity.Movimiento;
import cl.empresa.depositocpc.entity.Ubicacion;
import cl.empresa.depositocpc.entity.Usuario;
import cl.empresa.depositocpc.enums.EstadoContenedor;
import cl.empresa.depositocpc.enums.TipoContenedor;
import cl.empresa.depositocpc.enums.TipoMovimiento;
import cl.empresa.depositocpc.exception.ConflictoException;
import cl.empresa.depositocpc.exception.DespachoBloqueadoException;
import cl.empresa.depositocpc.exception.PeticionInvalidaException;
import cl.empresa.depositocpc.exception.PosicionLlenaException;
import cl.empresa.depositocpc.exception.RecursoNoEncontradoException;
import cl.empresa.depositocpc.mapper.ContenedorMapper;
import cl.empresa.depositocpc.repository.ContenedorRepository;
import cl.empresa.depositocpc.repository.MovimientoRepository;
import cl.empresa.depositocpc.repository.UbicacionRepository;
import cl.empresa.depositocpc.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContenedorService {

    public static final int DIAS_UMBRAL_ALERTA = 5;

    private final ContenedorRepository contenedorRepository;
    private final UbicacionRepository ubicacionRepository;
    private final MovimientoRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContenedorMapper contenedorMapper;

    @Value("${patio.altura-maxima-apilamiento:3}")
    int alturaMaxima;

    @Transactional
    public ContenedorResponseDTO ingresar(ContenedorRequestDTO peticion) {
        String numeroNormalizado = normalizarNumero(peticion.numeroContenedor());
        validarCoherenciaReefer(peticion.tipo(), peticion.reeferConectado());

        if (contenedorRepository.existsByNumeroContenedorIgnoreCase(numeroNormalizado)) {
            throw new ConflictoException("Ya existe un contenedor con el número " + numeroNormalizado);
        }

        Ubicacion ubicacion = buscarUbicacion(peticion.ubicacionId());
        int nivel = calcularSiguienteNivel(ubicacion);

        Contenedor contenedor = new Contenedor();
        contenedor.setNumeroContenedor(numeroNormalizado);
        contenedor.setTipo(peticion.tipo());
        contenedor.setTamano(peticion.tamano());
        contenedor.setReeferConectado(peticion.reeferConectado());
        contenedor.setCondicion(peticion.condicion());
        contenedor.setEstado(EstadoContenedor.EN_DEPOSITO);
        contenedor.setNivel(nivel);
        contenedor.setFechaIngreso(peticion.fechaIngreso() != null ? peticion.fechaIngreso() : OffsetDateTime.now());
        contenedor.setUbicacion(ubicacion);
        contenedor = contenedorRepository.save(contenedor);

        registrarMovimiento(contenedor, ubicacion, TipoMovimiento.INGRESO);

        return armarRespuesta(contenedor);
    }

    @Transactional(readOnly = true)
    public NivelActualDTO obtenerNivelActual(String bloque, String bahia, String fila) {
        int nivelMaximo = contenedorRepository.nivelMaximoEnPosicion(bloque, bahia, fila);
        int siguiente = nivelMaximo + 1;
        boolean llena = siguiente > alturaMaxima;
        return new NivelActualDTO(bloque, bahia, fila, nivelMaximo, siguiente, llena);
    }

    @Transactional(readOnly = true)
    public PaginaDTO<ContenedorResponseDTO> listar(EstadoContenedor estado,
                                                   TipoContenedor tipo,
                                                   UUID ubicacionId,
                                                   String bloque,
                                                   String bahia,
                                                   String fila,
                                                   java.time.LocalDate fechaDesde,
                                                   java.time.LocalDate fechaHasta,
                                                   Pageable pageable) {
        Specification<Contenedor> especificacion = Specification.where(null);

        if (estado != null) {
            especificacion = especificacion.and((raiz, consulta, cb) ->
                    cb.equal(raiz.get("estado"), estado));
        }
        if (tipo != null) {
            especificacion = especificacion.and((raiz, consulta, cb) ->
                    cb.equal(raiz.get("tipo"), tipo));
        }
        if (ubicacionId != null) {
            especificacion = especificacion.and((raiz, consulta, cb) ->
                    cb.equal(raiz.get("ubicacion").get("id"), ubicacionId));
        }
        if (bloque != null && !bloque.isBlank()) {
            especificacion = especificacion.and((raiz, consulta, cb) ->
                    cb.equal(raiz.get("ubicacion").get("bloque"), bloque));
        }
        if (bahia != null && !bahia.isBlank()) {
            especificacion = especificacion.and((raiz, consulta, cb) ->
                    cb.equal(raiz.get("ubicacion").get("bahia"), bahia));
        }
        if (fila != null && !fila.isBlank()) {
            especificacion = especificacion.and((raiz, consulta, cb) ->
                    cb.equal(raiz.get("ubicacion").get("fila"), fila));
        }
        if (fechaDesde != null) {
            especificacion = especificacion.and((raiz, consulta, cb) ->
                    cb.greaterThanOrEqualTo(raiz.get("fechaIngreso"),
                            fechaDesde.atStartOfDay()));
        }
        if (fechaHasta != null) {
            especificacion = especificacion.and((raiz, consulta, cb) ->
                    cb.lessThan(raiz.get("fechaIngreso"),
                            fechaHasta.plusDays(1).atStartOfDay()));
        }

        Page<Contenedor> pagina = contenedorRepository.findAll(especificacion, pageable);
        List<ContenedorResponseDTO> contenido = pagina.getContent().stream()
                .map(this::armarRespuesta)
                .toList();

        return new PaginaDTO<>(contenido, pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages(), pagina.isLast());
    }

    @Transactional(readOnly = true)
    public ContenedorResponseDTO obtenerPorId(UUID id) {
        return armarRespuesta(buscarEntidad(id));
    }

    @Transactional
    public ContenedorResponseDTO actualizar(UUID id, ContenedorActualizacionDTO peticion) {
        Contenedor contenedor = buscarEntidad(id);
        if (contenedor.getEstado() == EstadoContenedor.DESPACHADO) {
            throw new ConflictoException("No se puede modificar un contenedor despachado");
        }

        if (peticion.tipo() != null) {
            Boolean reeferEfectivo = peticion.reeferConectado() != null
                    ? peticion.reeferConectado()
                    : contenedor.getReeferConectado();
            validarCoherenciaReefer(peticion.tipo(), reeferEfectivo);
            contenedor.setTipo(peticion.tipo());
        }
        if (peticion.tamano() != null) {
            contenedor.setTamano(peticion.tamano());
        }
        if (peticion.reeferConectado() != null) {
            validarCoherenciaReefer(contenedor.getTipo(), peticion.reeferConectado());
            contenedor.setReeferConectado(peticion.reeferConectado());
        }
        if (peticion.condicion() != null) {
            contenedor.setCondicion(peticion.condicion());
        }

        boolean cambioUbicacion = false;
        Ubicacion nuevaUbicacion = contenedor.getUbicacion();
        if (peticion.ubicacionId() != null && !peticion.ubicacionId().equals(nuevaUbicacion.getId())) {
            nuevaUbicacion = buscarUbicacion(peticion.ubicacionId());
            int nuevoNivel = calcularSiguienteNivel(nuevaUbicacion);
            contenedor.setUbicacion(nuevaUbicacion);
            contenedor.setNivel(nuevoNivel);
            cambioUbicacion = true;
        }

        contenedor = contenedorRepository.save(contenedor);

        if (cambioUbicacion) {
            registrarMovimiento(contenedor, nuevaUbicacion, TipoMovimiento.REUBICACION);
        }

        return armarRespuesta(contenedor);
    }

    @Transactional
    public ContenedorResponseDTO despachar(UUID id) {
        Contenedor contenedor = buscarEntidad(id);
        if (contenedor.getEstado() == EstadoContenedor.DESPACHADO) {
            throw new ConflictoException("El contenedor ya fue despachado");
        }

        // Validación LIFO: verificar que no hay contenedores encima
        List<Contenedor> encima = contenedorRepository.contenedoresEncima(
                contenedor.getUbicacion().getBloque(),
                contenedor.getUbicacion().getBahia(),
                contenedor.getUbicacion().getFila(),
                contenedor.getNivel());

        if (!encima.isEmpty()) {
            throw new DespachoBloqueadoException(
                    "No se puede despachar " + contenedor.getNumeroContenedor()
                            + ". Hay " + encima.size() + " contenedor(es) encima en la posición "
                            + contenedor.getUbicacion().getBloque() + "-"
                            + contenedor.getUbicacion().getBahia() + "-"
                            + contenedor.getUbicacion().getFila()
                            + ". Debe reubicarlos primero.",
                    encima.size());
        }

        contenedor.setEstado(EstadoContenedor.DESPACHADO);
        contenedor = contenedorRepository.save(contenedor);

        registrarMovimiento(contenedor, contenedor.getUbicacion(), TipoMovimiento.SALIDA);

        return armarRespuesta(contenedor);
    }

    @Transactional(readOnly = true)
    public List<ContenedorResponseDTO> listarAlertas() {
        OffsetDateTime umbral = OffsetDateTime.now().minusDays(DIAS_UMBRAL_ALERTA);
        return contenedorRepository
                .findByEstadoAndFechaIngresoLessThanEqual(EstadoContenedor.EN_DEPOSITO, umbral)
                .stream()
                .sorted((a, b) -> a.getFechaIngreso().compareTo(b.getFechaIngreso()))
                .map(this::armarRespuesta)
                .toList();
    }

    private ContenedorResponseDTO armarRespuesta(Contenedor contenedor) {
        long diasEnDeposito = calcularDiasEnDeposito(contenedor);
        boolean enAlerta = contenedor.getEstado() == EstadoContenedor.EN_DEPOSITO
                && diasEnDeposito >= DIAS_UMBRAL_ALERTA;
        return contenedorMapper.toDto(contenedor, diasEnDeposito, enAlerta);
    }

    private long calcularDiasEnDeposito(Contenedor contenedor) {
        return ChronoUnit.DAYS.between(contenedor.getFechaIngreso(), OffsetDateTime.now());
    }

    /**
     * Calcula el siguiente nivel disponible en una posición.
     * Lanza PosicionLlenaException si la altura máxima fue alcanzada.
     */
    private int calcularSiguienteNivel(Ubicacion ubicacion) {
        int nivelMaximo = contenedorRepository.nivelMaximoEnPosicion(
                ubicacion.getBloque(), ubicacion.getBahia(), ubicacion.getFila());
        int siguiente = nivelMaximo + 1;
        if (siguiente > alturaMaxima) {
            throw new PosicionLlenaException(
                    "La posición " + ubicacion.getBloque() + "-" + ubicacion.getBahia()
                            + "-" + ubicacion.getFila() + " está llena (altura máxima: " + alturaMaxima + ")");
        }
        return siguiente;
    }

    private void registrarMovimiento(Contenedor contenedor, Ubicacion ubicacion, TipoMovimiento tipo) {
        Movimiento movimiento = new Movimiento();
        movimiento.setContenedor(contenedor);
        movimiento.setUbicacion(ubicacion);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setFecha(OffsetDateTime.now());
        movimiento.setRegistradoPor(obtenerUsuarioAutenticado());
        movimientoRepository.save(movimiento);
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        String email = autenticacion.getName();
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario autenticado no existe: " + email));
    }

    private void validarCoherenciaReefer(TipoContenedor tipo, Boolean reeferConectado) {
        if (tipo != TipoContenedor.REEFER && reeferConectado != null) {
            throw new PeticionInvalidaException(
                    "Solo se puede indicar 'reeferConectado' cuando el tipo de contenedor es REEFER",
                    "reeferConectado");
        }
    }

    private String normalizarNumero(String numeroContenedor) {
        return numeroContenedor.trim().toUpperCase(Locale.ROOT);
    }

    private Contenedor buscarEntidad(UUID id) {
        return contenedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Contenedor", id));
    }

    private Ubicacion buscarUbicacion(UUID ubicacionId) {
        return ubicacionRepository.findById(ubicacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ubicación", ubicacionId));
    }
}
