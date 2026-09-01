package cl.empresa.depositocpc.service;

import cl.empresa.depositocpc.dto.ContenedorActualizacionDTO;
import cl.empresa.depositocpc.dto.ContenedorRequestDTO;
import cl.empresa.depositocpc.dto.ContenedorResponseDTO;
import cl.empresa.depositocpc.entity.Contenedor;
import cl.empresa.depositocpc.entity.Movimiento;
import cl.empresa.depositocpc.entity.Ubicacion;
import cl.empresa.depositocpc.entity.Usuario;
import cl.empresa.depositocpc.enums.Condicion;
import cl.empresa.depositocpc.enums.EstadoContenedor;
import cl.empresa.depositocpc.enums.TamanoContenedor;
import cl.empresa.depositocpc.enums.TipoContenedor;
import cl.empresa.depositocpc.enums.TipoMovimiento;
import cl.empresa.depositocpc.exception.ConflictoException;
import cl.empresa.depositocpc.exception.DespachoBloqueadoException;
import cl.empresa.depositocpc.exception.PeticionInvalidaException;
import cl.empresa.depositocpc.exception.PosicionLlenaException;
import cl.empresa.depositocpc.mapper.ContenedorMapperImpl;
import cl.empresa.depositocpc.repository.ContenedorRepository;
import cl.empresa.depositocpc.repository.MovimientoRepository;
import cl.empresa.depositocpc.repository.UbicacionRepository;
import cl.empresa.depositocpc.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContenedorServiceTest {

    private static final String EMAIL_USUARIO = "admin@empresa.cl";
    private static final int ALTURA_MAXIMA = 3;

    @Mock
    private ContenedorRepository contenedorRepository;

    @Mock
    private UbicacionRepository ubicacionRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Captor
    private ArgumentCaptor<Movimiento> captorMovimiento;

    private ContenedorService contenedorService;

    private Ubicacion ubicacionA;
    private Ubicacion ubicacionB;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        contenedorService = new ContenedorService(
                contenedorRepository,
                ubicacionRepository,
                movimientoRepository,
                usuarioRepository,
                new ContenedorMapperImpl());
        contenedorService.alturaMaxima = ALTURA_MAXIMA;

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL_USUARIO, null));

        ubicacionA = new Ubicacion();
        ubicacionA.setId(UUID.randomUUID());
        ubicacionA.setBloque("A");
        ubicacionA.setBahia("01");
        ubicacionA.setFila("1");

        ubicacionB = new Ubicacion();
        ubicacionB.setId(UUID.randomUUID());
        ubicacionB.setBloque("B");
        ubicacionB.setBahia("02");
        ubicacionB.setFila("2");

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail(EMAIL_USUARIO);
        usuario.setPasswordHash("hash");
        usuario.setActivo(true);
        usuario.setRol(cl.empresa.depositocpc.enums.RolUsuario.OPERADOR);
        usuario.setCreadoEn(OffsetDateTime.now());
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("ingresar: crea el contenedor con nivel 1 en posición vacía")
    void ingresarCreaContenedorNivelUno() {
        when(contenedorRepository.existsByNumeroContenedorIgnoreCase("CSQU3054383")).thenReturn(false);
        when(ubicacionRepository.findById(ubicacionA.getId())).thenReturn(Optional.of(ubicacionA));
        when(usuarioRepository.findByEmailIgnoreCase(EMAIL_USUARIO)).thenReturn(Optional.of(usuario));
        when(contenedorRepository.nivelMaximoEnPosicion("A", "01", "1")).thenReturn(0);
        when(contenedorRepository.save(any(Contenedor.class)))
                .thenAnswer(invocacion -> {
                    Contenedor guardado = invocacion.getArgument(0);
                    guardado.setId(UUID.randomUUID());
                    return guardado;
                });

        ContenedorRequestDTO peticion = new ContenedorRequestDTO(
                "csqu3054383", TipoContenedor.DRY, TamanoContenedor.VEINTE,
                null, Condicion.LLENO, ubicacionA.getId(), OffsetDateTime.now());

        ContenedorResponseDTO respuesta = contenedorService.ingresar(peticion);

        assertThat(respuesta.numeroContenedor()).isEqualTo("CSQU3054383");
        assertThat(respuesta.estado()).isEqualTo(EstadoContenedor.EN_DEPOSITO);
        assertThat(respuesta.nivel()).isEqualTo(1);
        assertThat(respuesta.tamano()).isEqualTo(TamanoContenedor.VEINTE);
        assertThat(respuesta.enAlerta()).isFalse();

        verify(movimientoRepository).save(captorMovimiento.capture());
        Movimiento movimiento = captorMovimiento.getValue();
        assertThat(movimiento.getTipoMovimiento()).isEqualTo(TipoMovimiento.INGRESO);
        assertThat(movimiento.getRegistradoPor()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("ingresar: asigna nivel 3 cuando hay 2 contenedores en la posición")
    void ingresarAsignaNivelTres() {
        when(contenedorRepository.existsByNumeroContenedorIgnoreCase("CSQU3054383")).thenReturn(false);
        when(ubicacionRepository.findById(ubicacionA.getId())).thenReturn(Optional.of(ubicacionA));
        when(usuarioRepository.findByEmailIgnoreCase(EMAIL_USUARIO)).thenReturn(Optional.of(usuario));
        when(contenedorRepository.nivelMaximoEnPosicion("A", "01", "1")).thenReturn(2);
        when(contenedorRepository.save(any(Contenedor.class)))
                .thenAnswer(invocacion -> {
                    Contenedor guardado = invocacion.getArgument(0);
                    guardado.setId(UUID.randomUUID());
                    return guardado;
                });

        ContenedorRequestDTO peticion = new ContenedorRequestDTO(
                "csqu3054383", TipoContenedor.DRY, TamanoContenedor.CUARENTA,
                null, Condicion.LLENO, ubicacionA.getId(), OffsetDateTime.now());

        ContenedorResponseDTO respuesta = contenedorService.ingresar(peticion);

        assertThat(respuesta.nivel()).isEqualTo(3);
        assertThat(respuesta.tamano()).isEqualTo(TamanoContenedor.CUARENTA);
    }

    @Test
    @DisplayName("ingresar: lanza PosicionLlenaException cuando la altura máxima es alcanzada")
    void ingresarLanzaPosicionLlena() {
        when(contenedorRepository.existsByNumeroContenedorIgnoreCase("CSQU3054383")).thenReturn(false);
        when(ubicacionRepository.findById(ubicacionA.getId())).thenReturn(Optional.of(ubicacionA));
        when(contenedorRepository.nivelMaximoEnPosicion("A", "01", "1")).thenReturn(3);

        ContenedorRequestDTO peticion = new ContenedorRequestDTO(
                "csqu3054383", TipoContenedor.DRY, TamanoContenedor.VEINTE,
                null, Condicion.LLENO, ubicacionA.getId(), OffsetDateTime.now());

        assertThatThrownBy(() -> contenedorService.ingresar(peticion))
                .isInstanceOf(PosicionLlenaException.class)
                .hasMessageContaining("llena");
    }

    @Test
    @DisplayName("ingresar: rechaza números de contenedor duplicados")
    void ingresarRechazaDuplicado() {
        when(contenedorRepository.existsByNumeroContenedorIgnoreCase("CSQU3054383")).thenReturn(true);

        ContenedorRequestDTO peticion = new ContenedorRequestDTO(
                "CSQU3054383", TipoContenedor.DRY, TamanoContenedor.VEINTE,
                null, Condicion.LLENO, ubicacionA.getId(), OffsetDateTime.now());

        assertThatThrownBy(() -> contenedorService.ingresar(peticion))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    @DisplayName("ingresar: rechaza reeferConectado cuando el tipo no es REEFER")
    void ingresarRechazaReeferIncoherente() {
        ContenedorRequestDTO peticion = new ContenedorRequestDTO(
                "CSQU3054383", TipoContenedor.DRY, TamanoContenedor.VEINTE,
                true, Condicion.LLENO, ubicacionA.getId(), OffsetDateTime.now());

        assertThatThrownBy(() -> contenedorService.ingresar(peticion))
                .isInstanceOf(PeticionInvalidaException.class);
    }

    @Test
    @DisplayName("actualizar: cambia la ubicación y recalcula el nivel automáticamente")
    void actualizarCambiaUbicacionYNivel() {
        Contenedor existente = crearContenedorEnDeposito();
        when(contenedorRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(ubicacionRepository.findById(ubicacionB.getId())).thenReturn(Optional.of(ubicacionB));
        when(usuarioRepository.findByEmailIgnoreCase(EMAIL_USUARIO)).thenReturn(Optional.of(usuario));
        when(contenedorRepository.nivelMaximoEnPosicion("B", "02", "2")).thenReturn(0);
        when(contenedorRepository.save(any(Contenedor.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        ContenedorActualizacionDTO peticion = new ContenedorActualizacionDTO(
                null, null, null, null, ubicacionB.getId());

        ContenedorResponseDTO respuesta = contenedorService.actualizar(existente.getId(), peticion);

        assertThat(respuesta.ubicacion().id()).isEqualTo(ubicacionB.getId());
        assertThat(respuesta.nivel()).isEqualTo(1);
        verify(movimientoRepository).save(captorMovimiento.capture());
        assertThat(captorMovimiento.getValue().getTipoMovimiento()).isEqualTo(TipoMovimiento.REUBICACION);
    }

    @Test
    @DisplayName("despachar: permite despachar si no hay contenedores encima (LIFO OK)")
    void despacharPermiteSiNoHayEncima() {
        Contenedor existente = crearContenedorEnDeposito();
        existente.setNivel(3);
        when(contenedorRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(contenedorRepository.contenedoresEncima("A", "01", "1", 3)).thenReturn(List.of());
        when(usuarioRepository.findByEmailIgnoreCase(EMAIL_USUARIO)).thenReturn(Optional.of(usuario));
        when(contenedorRepository.save(any(Contenedor.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        ContenedorResponseDTO respuesta = contenedorService.despachar(existente.getId());

        assertThat(respuesta.estado()).isEqualTo(EstadoContenedor.DESPACHADO);
        verify(movimientoRepository).save(captorMovimiento.capture());
        assertThat(captorMovimiento.getValue().getTipoMovimiento()).isEqualTo(TipoMovimiento.SALIDA);
    }

    @Test
    @DisplayName("despachar: bloquea si hay contenedores encima (viola LIFO)")
    void despacharBloqueaPorLIFO() {
        Contenedor existente = crearContenedorEnDeposito();
        existente.setNivel(1);

        Contenedor encima = new Contenedor();
        encima.setId(UUID.randomUUID());
        encima.setNivel(2);

        when(contenedorRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(contenedorRepository.contenedoresEncima("A", "01", "1", 1)).thenReturn(List.of(encima));

        assertThatThrownBy(() -> contenedorService.despachar(existente.getId()))
                .isInstanceOf(DespachoBloqueadoException.class)
                .hasMessageContaining("1 contenedor(es) encima");
    }

    @Test
    @DisplayName("despachar: rechaza despachar dos veces el mismo contenedor")
    void despacharDosVecesLanzaConflicto() {
        Contenedor existente = crearContenedorEnDeposito();
        existente.setEstado(EstadoContenedor.DESPACHADO);
        when(contenedorRepository.findById(existente.getId())).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> contenedorService.despachar(existente.getId()))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    @DisplayName("listarAlertas: marca enAlerta solo a los EN_DEPOSITO con 5+ días")
    void listarAlertasMarcaAntiguosEnDeposito() {
        Contenedor antiguo = crearContenedorEnDeposito();
        antiguo.setFechaIngreso(OffsetDateTime.now().minusDays(6));

        Contenedor despachadoViejo = crearContenedorEnDeposito();
        despachadoViejo.setFechaIngreso(OffsetDateTime.now().minusDays(30));
        despachadoViejo.setEstado(EstadoContenedor.DESPACHADO);

        when(contenedorRepository.findByEstadoAndFechaIngresoLessThanEqual(
                eq(EstadoContenedor.EN_DEPOSITO), any(OffsetDateTime.class)))
                .thenReturn(List.of(antiguo, despachadoViejo));

        List<ContenedorResponseDTO> alertas = contenedorService.listarAlertas();

        assertThat(alertas).hasSize(2);
        assertThat(alertas.getFirst().enAlerta()).isFalse();

        ContenedorResponseDTO antiguoEnDeposito = alertas.getLast();
        assertThat(antiguoEnDeposito.enAlerta()).isTrue();
        assertThat(antiguoEnDeposito.diasEnDeposito()).isEqualTo(6);
    }

    @Test
    @DisplayName("los días en depósito se truncan (30 horas equivale a 1 día)")
    void diasEnDepositoSeTruncan() {
        Contenedor existente = crearContenedorEnDeposito();
        existente.setFechaIngreso(OffsetDateTime.now().minusHours(30));
        when(contenedorRepository.findById(existente.getId())).thenReturn(Optional.of(existente));

        ContenedorResponseDTO respuesta = contenedorService.obtenerPorId(existente.getId());

        assertThat(respuesta.diasEnDeposito()).isEqualTo(1);
        assertThat(respuesta.enAlerta()).isFalse();
    }

    private Contenedor crearContenedorEnDeposito() {
        Contenedor contenedor = new Contenedor();
        contenedor.setId(UUID.randomUUID());
        contenedor.setNumeroContenedor("MSKU1234565");
        contenedor.setTipo(TipoContenedor.DRY);
        contenedor.setTamano(TamanoContenedor.VEINTE);
        contenedor.setReeferConectado(null);
        contenedor.setCondicion(Condicion.LLENO);
        contenedor.setEstado(EstadoContenedor.EN_DEPOSITO);
        contenedor.setNivel(1);
        contenedor.setFechaIngreso(OffsetDateTime.now());
        contenedor.setUbicacion(ubicacionA);
        return contenedor;
    }
}
