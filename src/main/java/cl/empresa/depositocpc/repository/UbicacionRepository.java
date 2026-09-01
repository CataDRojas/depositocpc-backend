package cl.empresa.depositocpc.repository;

import cl.empresa.depositocpc.entity.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UbicacionRepository extends JpaRepository<Ubicacion, UUID> {

    boolean existsByBloqueAndBahiaAndFila(String bloque, String bahia, String fila);

    @Query("SELECT DISTINCT u.bloque FROM Ubicacion u ORDER BY u.bloque")
    List<String> findBloquesUnicos();

    @Query("SELECT DISTINCT u.bahia FROM Ubicacion u WHERE u.bloque = :bloque ORDER BY u.bahia")
    List<String> findBahiasPorBloque(@Param("bloque") String bloque);

    @Query("SELECT DISTINCT u.fila FROM Ubicacion u WHERE u.bloque = :bloque AND u.bahia = :bahia ORDER BY u.fila")
    List<String> findFilasPorBloqueYBahia(@Param("bloque") String bloque, @Param("bahia") String bahia);

    Optional<Ubicacion> findByBloqueAndBahiaAndFila(String bloque, String bahia, String fila);
}
