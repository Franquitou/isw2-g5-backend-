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
 * Partido de una jornada del torneo. Se mapea sobre la tabla {@code partido} de {@code schema.sql}.
 *
 * <p>Las referencias a torneo, equipos, cancha y árbitro se guardan como identificadores, igual que en
 * {@code Equipo}: las reglas que las relacionan se validan en {@code PartidoService}. La fecha y hora de inicio
 * está expresada en la zona de la liga ({@code HorarioPartido.ZONA_LIGA}). El resultado no se guarda: se calcula
 * a partir de los eventos (RN-06).</p>
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

    /** Constructor requerido por JPA para reconstruir la entidad desde la base. */
    protected Partido() {
    }

    /**
     * Crea un partido nuevo, que siempre nace {@link EstadoPartido#PROGRAMADO}.
     *
     * @param torneoId torneo al que pertenece
     * @param numeroFecha jornada, desde 1
     * @param equipoLocalId equipo local
     * @param equipoVisitanteId equipo visitante
     * @param canchaId cancha asignada
     * @param arbitroId árbitro asignado
     * @param fechaHoraInicio inicio en la zona de la liga
     */
    public Partido(Integer torneoId, Integer numeroFecha, Integer equipoLocalId, Integer equipoVisitanteId,
            Integer canchaId, Integer arbitroId, LocalDateTime fechaHoraInicio) {
        this.estado = EstadoPartido.PROGRAMADO;
        asignar(torneoId, numeroFecha, equipoLocalId, equipoVisitanteId, canchaId, arbitroId, fechaHoraInicio);
    }

    /**
     * Reemplaza los datos de programación. Solo debe usarse mientras el partido está
     * {@link EstadoPartido#PROGRAMADO}; esa regla la controla {@code PartidoService}.
     *
     * @param torneoId torneo al que pertenece
     * @param numeroFecha jornada, desde 1
     * @param equipoLocalId equipo local
     * @param equipoVisitanteId equipo visitante
     * @param canchaId cancha asignada
     * @param arbitroId árbitro asignado
     * @param fechaHoraInicio inicio en la zona de la liga
     */
    public void reprogramar(Integer torneoId, Integer numeroFecha, Integer equipoLocalId, Integer equipoVisitanteId,
            Integer canchaId, Integer arbitroId, LocalDateTime fechaHoraInicio) {
        asignar(torneoId, numeroFecha, equipoLocalId, equipoVisitanteId, canchaId, arbitroId, fechaHoraInicio);
    }
    /**
     * Cambia el estado respetando las transiciones de RN-05 ({@link EstadoPartido#puedeCambiarA}).
     * {@code PartidoService} valida antes y responde 409; esta comprobación evita que la entidad
     * quede en un estado inválido aunque se la use desde otro lugar.
     *
     * @param nuevo estado de destino
     * @throws IllegalStateException si la transición no está permitida
     */
    public void cambiarEstado(EstadoPartido nuevo) {
        if (!estado.puedeCambiarA(nuevo)) {
            throw new IllegalStateException("Transición no permitida: " + estado + " -> " + nuevo);
        }
        this.estado = nuevo;
    }

    private void asignar(Integer torneoId, Integer numeroFecha, Integer equipoLocalId, Integer equipoVisitanteId,
            Integer canchaId, Integer arbitroId, LocalDateTime fechaHoraInicio) {
        this.torneoId = torneoId;
        this.numeroFecha = numeroFecha;
        this.equipoLocalId = equipoLocalId;
        this.equipoVisitanteId = equipoVisitanteId;
        this.canchaId = canchaId;
        this.arbitroId = arbitroId;
        this.fechaHoraInicio = fechaHoraInicio;
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
