package cl.empresa.depositocpc.repository;

import cl.empresa.depositocpc.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovimientoRepository extends JpaRepository<Movimiento, UUID> {

    List<Movimiento> findByContenedorIdOrderByFechaDesc(UUID contenedorId);
}
