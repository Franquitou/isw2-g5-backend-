package isw2_G5.liga_amateur.evento.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Evento de un partido (gol, amarilla o roja) registrado por un delegado o un administrador (RF-08).
 * Se mapea contra la tabla {@code evento_partido} de {@code schema.sql}. Para corregir un error
 * el evento se borra y se vuelve a cargar (RF-09).
 */
@Entity
@Table(name = "evento_partido")
public class EventoPartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "partido_id", nullable = false)
    private Integer partidoId;

    @Column(name = "jugador_id", nullable = false)
    private Integer jugadorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoEvento tipo;

    @Column(name = "minuto", nullable = false)
    private Integer minuto;

    /** Constructor requerido por JPA para reconstruir la entidad desde la base. */
    protected EventoPartido() {
    }

    /**
     * Crea un evento para un partido.
     *
     * @param partidoId partido en el que ocurrió
     * @param jugadorId jugador que protagonizó el evento
     * @param tipo gol, amarilla o roja
     * @param minuto minuto del partido, de 1 a 120
     */
    public EventoPartido(Integer partidoId, Integer jugadorId, TipoEvento tipo, Integer minuto) {
        this.partidoId = partidoId;
        this.jugadorId = jugadorId;
        this.tipo = tipo;
        this.minuto = minuto;
    }

    public Integer getId() {
        return id;
    }

    public Integer getPartidoId() {
        return partidoId;
    }

    public Integer getJugadorId() {
        return jugadorId;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public Integer getMinuto() {
        return minuto;
    }
}
