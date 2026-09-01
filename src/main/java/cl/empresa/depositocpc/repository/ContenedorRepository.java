package cl.empresa.depositocpc.repository;

import cl.empresa.depositocpc.entity.Contenedor;
import cl.empresa.depositocpc.enums.EstadoContenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ContenedorRepository extends JpaRepository<Contenedor, UUID>, JpaSpecificationExecutor<Contenedor> {

    boolean existsByNumeroContenedorIgnoreCase(String numeroContenedor);

    List<Contenedor> findByEstadoAndFechaIngresoLessThanEqual(EstadoContenedor estado, OffsetDateTime fecha);

    /**
     * Cuenta cuántos contenedores hay apilados en una posición exacta (bloque+bahía+fila).
     */
    @Query("""
            SELECT COUNT(c) FROM Contenedor c
            WHERE c.ubicacion.bloque = :bloque
              AND c.ubicacion.bahia = :bahia
              AND c.ubicacion.fila = :fila
              AND c.estado = cl.empresa.depositocpc.enums.EstadoContenedor.EN_DEPOSITO
            """)
    long contarEnPosicion(@Param("bloque") String bloque,
                          @Param("bahia") String bahia,
                          @Param("fila") String fila);

    /**
     * Retorna el nivel más alto ocupado en una posición exacta.
     */
    @Query("""
            SELECT COALESCE(MAX(c.nivel), 0) FROM Contenedor c
            WHERE c.ubicacion.bloque = :bloque
              AND c.ubicacion.bahia = :bahia
              AND c.ubicacion.fila = :fila
              AND c.estado = cl.empresa.depositocpc.enums.EstadoContenedor.EN_DEPOSITO
            """)
    int nivelMaximoEnPosicion(@Param("bloque") String bloque,
                              @Param("bahia") String bahia,
                              @Param("fila") String fila);

    /**
     * Retorna los contenedores que están encima de un nivel dado en una posición.
     */
    @Query("""
            SELECT c FROM Contenedor c
            WHERE c.ubicacion.bloque = :bloque
              AND c.ubicacion.bahia = :bahia
              AND c.ubicacion.fila = :fila
              AND c.nivel > :nivel
              AND c.estado = cl.empresa.depositocpc.enums.EstadoContenedor.EN_DEPOSITO
            ORDER BY c.nivel DESC
            """)
    List<Contenedor> contenedoresEncima(@Param("bloque") String bloque,
                                        @Param("bahia") String bahia,
                                        @Param("fila") String fila,
                                        @Param("nivel") int nivel);
}
