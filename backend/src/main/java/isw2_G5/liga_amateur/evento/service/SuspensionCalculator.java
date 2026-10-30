package isw2_G5.liga_amateur.evento.service;

import isw2_G5.liga_amateur.evento.entity.EventoPartido;
import isw2_G5.liga_amateur.evento.entity.MotivoSuspension;
import isw2_G5.liga_amateur.evento.entity.TipoEvento;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Regla de suspensión de RF-12: un jugador queda suspendido para la fecha siguiente si en un mismo
 * partido recibió una roja o dos amarillas. Las amarillas no se acumulan entre partidos, por eso la
 * regla se aplica a los eventos de un solo partido. No depende de Spring ni de la base de datos.
 */
public final class SuspensionCalculator {

    private SuspensionCalculator() {
    }

    /**
     * Determina qué jugadores quedan sancionados por lo ocurrido en un partido.
     * Si un jugador tiene roja y además dos amarillas, el motivo informado es {@code ROJA}.
     *
     * @param eventosDeUnPartido eventos de un único partido; los goles se ignoran
     * @return id de cada jugador sancionado con su motivo, ordenado por id de jugador
     */
    public static Map<Integer, MotivoSuspension> calcular(List<EventoPartido> eventosDeUnPartido) {
        Objects.requireNonNull(eventosDeUnPartido, "eventosDeUnPartido");

        Map<Integer, MotivoSuspension> sanciones = new TreeMap<>();
        Map<Integer, Integer> amarillas = new HashMap<>();
        for (EventoPartido evento : eventosDeUnPartido) {
            Integer jugadorId = evento.getJugadorId();
            if (evento.getTipo() == TipoEvento.ROJA) {
                sanciones.put(jugadorId, MotivoSuspension.ROJA);
            } else if (evento.getTipo() == TipoEvento.AMARILLA) {
                int total = amarillas.merge(jugadorId, 1, Integer::sum);
                if (total >= 2) {
                    sanciones.putIfAbsent(jugadorId, MotivoSuspension.DOBLE_AMARILLA);
                }
            }
        }
        return sanciones;
    }
}