package isw2_G5.liga_amateur.evento.service;

import isw2_G5.liga_amateur.evento.dto.Marcador;
import java.util.Collection;
import java.util.Objects;

/**
 * Regla pura del marcador (RN-06): cada gol se asigna al equipo del jugador que lo convirtió.
 * No existen goles en contra; un gol de un equipo ajeno al partido se ignora.
 */
public final class MarcadorCalculator {

    private MarcadorCalculator() {
    }

    /**
     * Calcula el marcador de un partido.
     *
     * @param equipoLocalId equipo local
     * @param equipoVisitanteId equipo visitante
     * @param equiposGoleadores un elemento por gol: el equipo del jugador que lo marcó
     * @return marcador; 0-0 si no hay goles
     */
    public static Marcador calcular(Integer equipoLocalId, Integer equipoVisitanteId,
            Collection<Integer> equiposGoleadores) {
        int local = 0;
        int visitante = 0;
        for (Integer equipo : equiposGoleadores) {
            if (Objects.equals(equipo, equipoLocalId)) {
                local++;
            } else if (Objects.equals(equipo, equipoVisitanteId)) {
                visitante++;
            }
        }
        return new Marcador(local, visitante);
    }
}
