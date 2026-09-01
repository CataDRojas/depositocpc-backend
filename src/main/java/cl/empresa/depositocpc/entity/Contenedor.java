package cl.empresa.depositocpc.entity;

import cl.empresa.depositocpc.enums.Condicion;
import cl.empresa.depositocpc.enums.EstadoContenedor;
import cl.empresa.depositocpc.enums.TamanoContenedor;
import cl.empresa.depositocpc.enums.TipoContenedor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "contenedor")
@Getter
@Setter
@NoArgsConstructor
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_contenedor", nullable = false, unique = true, length = 11)
    private String numeroContenedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoContenedor tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TamanoContenedor tamano;

    /**
     * Solo aplica cuando tipo = REEFER; en cualquier otro caso debe ser null.
     */
    @Column(name = "reefer_conectado")
    private Boolean reeferConectado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Condicion condicion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoContenedor estado;

    /**
     * Nivel de apilamiento (altura) calculado automáticamente por el sistema
     * según cuántos contenedores hay en la misma ubicación base.
     */
    @Column(nullable = false)
    private Integer nivel;

    @Column(name = "fecha_ingreso", nullable = false)
    private OffsetDateTime fechaIngreso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private Ubicacion ubicacion;

    @PrePersist
    void alCrear() {
        if (estado == null) {
            estado = EstadoContenedor.EN_DEPOSITO;
        }
        if (fechaIngreso == null) {
            fechaIngreso = OffsetDateTime.now();
        }
    }
}
