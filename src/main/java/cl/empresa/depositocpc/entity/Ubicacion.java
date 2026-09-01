package cl.empresa.depositocpc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Ubicación física dentro del depósito: bloque + bahía + fila.
 * La altura (nivel) se calcula automáticamente según cuántos
 * contenedores estén apilados en esa posición.
 */
@Entity
@Table(name = "ubicacion", uniqueConstraints = {
        @UniqueConstraint(name = "uq_ubicacion", columnNames = {"bloque", "bahia", "fila"})
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"bloque", "bahia", "fila"})
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 10)
    private String bloque;

    @Column(nullable = false, length = 10)
    private String bahia;

    @Column(nullable = false, length = 10)
    private String fila;
}
