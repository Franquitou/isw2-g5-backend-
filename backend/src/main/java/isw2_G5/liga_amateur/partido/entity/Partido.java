package isw2_G5.liga_amateur.partido.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Partido de un torneo. Mapea la tabla {@code partido} de {@code schema.sql}.
 *
 * <p>PROVISORIA: el módulo de programación (José, C) es el dueño de esta entidad. Esta versión solo existe
 * para que eventos, tabla y suspendidos puedan leer partidos; al integrar, se conserva la de José.</p>
 */
@Entity
@Table(name = "partido")
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "torneo_id", nullable = false)
    private Integer torneoId;

    @Column(name = "numero_fecha", nullable = false)
    private Integer numeroFecha;

    @Column(name = "equipo_local_id", nullable = false)
    private Integer equipoLocalId;

    @Column(name = "equipo_visitante_id", nullable = false)
    private Integer equipoVisitanteId;

    @Column(name = "cancha_id", nullable = false)
    private Integer canchaId;

    @Column(name = "arbitro_id", nullable = false)
    private Integer arbitroId;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPartido estado;

    /** Constructor requerido por JPA. */
    protected Partido() {
    }

    /** Crea un partido con todos sus datos; usado por las pruebas hasta que exista el módulo de programación. */
    public Partido(Integer torneoId, Integer numeroFecha, Integer equipoLocalId, Integer equipoVisitanteId,
            Integer canchaId, Integer arbitroId, LocalDateTime fechaHoraInicio, EstadoPartido estado) {
        this.torneoId = torneoId;
        this.numeroFecha = numeroFecha;
        this.equipoLocalId = equipoLocalId;
        this.equipoVisitanteId = equipoVisitanteId;
        this.canchaId = canchaId;
        this.arbitroId = arbitroId;
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = estado;
    }

    public Integer getId() {
        return id;
    }

    public Integer getTorneoId() {
        return torneoId;
    }

    public Integer getNumeroFecha() {
        return numeroFecha;
    }

    public Integer getEquipoLocalId() {
        return equipoLocalId;
    }

    public Integer getEquipoVisitanteId() {
        return equipoVisitanteId;
    }

    public Integer getCanchaId() {
        return canchaId;
    }

    public Integer getArbitroId() {
        return arbitroId;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public EstadoPartido getEstado() {
        return estado;
    }
}
