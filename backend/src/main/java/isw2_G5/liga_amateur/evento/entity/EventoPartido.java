package isw2_G5.liga_amateur.evento.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Evento (gol o tarjeta) de un partido. Se mapea contra la tabla {@code evento_partido} de {@code schema.sql}. */
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

    /** Constructor requerido por JPA. */
    protected EventoPartido() {
    }

    /**
     * Crea un evento nuevo.
     *
     * @param partidoId partido en el que ocurre
     * @param jugadorId jugador protagonista
     * @param tipo gol, amarilla o roja
     * @param minuto minuto entre 1 y 120
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
