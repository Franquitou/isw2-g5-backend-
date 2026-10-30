package isw2_G5.liga_amateur.evento.service;

import isw2_G5.liga_amateur.evento.entity.MotivoSuspension;
import isw2_G5.liga_amateur.evento.entity.TipoEvento;
import isw2_G5.liga_amateur.evento.repository.TarjetaDeJugador;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Regla pura de suspensión (RN-03/RN-04, RF-12): una roja, o dos amarillas <b>en el mismo partido</b>,
 * suspenden al jugador para la fecha siguiente. Las amarillas no se acumulan entre partidos.
 * Si un jugador tiene roja y dos amarillas en el mismo partido, el motivo es {@code ROJA}.
 */
public final class SuspensionCalculator {

    /**
     * Sanción resultante para un jugador.
     *
     * @param jugadorId jugador sancionado
     * @param equipoId equipo del jugador
     * @param motivo roja o doble amarilla
     */
    public record Sancion(Integer jugadorId, Integer equipoId, MotivoSuspension motivo) {
    }

    private SuspensionCalculator() {
    }

    /**
     * Determina quién queda suspendido a partir de las tarjetas de partidos finalizados.
     *
     * @param tarjetas amarillas y rojas de los partidos de la jornada anterior
     * @return una sanción por jugador sancionado, ordenada por equipo y jugador
     */
    public static List<Sancion> calcular(Collection<TarjetaDeJugador> tarjetas) {
        Map<String, int[]> conteos = new HashMap<>();   // clave partido|jugador -> {amarillas, rojas}
        Map<String, TarjetaDeJugador> referencia = new HashMap<>();
        for (TarjetaDeJugador tarjeta : tarjetas) {
            String clave = tarjeta.partidoId() + "|" + tarjeta.jugadorId();
            int[] conteo = conteos.computeIfAbsent(clave, k -> new int[2]);
            referencia.putIfAbsent(clave, tarjeta);
            if (tarjeta.tipo() == TipoEvento.AMARILLA) {
                conteo[0]++;
            } else if (tarjeta.tipo() == TipoEvento.ROJA) {
                conteo[1]++;
            }
        }
        Map<Integer, Sancion> porJugador = new HashMap<>();
        conteos.forEach((clave, conteo) -> {
            MotivoSuspension motivo = conteo[1] > 0 ? MotivoSuspension.ROJA
                    : conteo[0] >= 2 ? MotivoSuspension.DOBLE_AMARILLA : null;
            if (motivo == null) {
                return;
            }
            TarjetaDeJugador base = referencia.get(clave);
            Sancion existente = porJugador.get(base.jugadorId());
            if (existente == null || motivo == MotivoSuspension.ROJA) {
                porJugador.put(base.jugadorId(), new Sancion(base.jugadorId(), base.equipoId(), motivo));
            }
        });
        return porJugador.values().stream()
                .sorted(Comparator.comparing(Sancion::equipoId).thenComparing(Sancion::jugadorId))
                .toList();
    }
}
