package isw2_G5.liga_amateur.evento.service;

import isw2_G5.liga_amateur.evento.dto.Marcador;
import java.util.List;
import java.util.Objects;

/**
 * Regla única del marcador (RN-06): cada gol suma al equipo del jugador que lo convirtió.
 * No hay goles en contra ni resultado cargado a mano. No depende de Spring ni de la base de datos.
 */
public final class MarcadorCalculator {

    private MarcadorCalculator() {
    }

    /**
     * Calcula el marcador a partir del equipo de cada autor de gol.
     *
     * @param equipoLocalId id del equipo local del partido
     * @param equipoVisitanteId id del equipo visitante del partido
     * @param equiposDeGoleadores un elemento por cada evento GOL: el id del equipo del jugador que lo hizo
     * @return marcador; 0-0 si no hay goles
     * @throws IllegalArgumentException si algún gol pertenece a un equipo que no juega el partido
     */
    public static Marcador calcular(Integer equipoLocalId, Integer equipoVisitanteId,
                                    List<Integer> equiposDeGoleadores) {
        Objects.requireNonNull(equipoLocalId, "equipoLocalId");
        Objects.requireNonNull(equipoVisitanteId, "equipoVisitanteId");
        Objects.requireNonNull(equiposDeGoleadores, "equiposDeGoleadores");

        int local = 0;
        int visitante = 0;
        for (Integer equipoId : equiposDeGoleadores) {
            if (equipoLocalId.equals(equipoId)) {
                local++;
            } else if (equipoVisitanteId.equals(equipoId)) {
                visitante++;
            } else {
                throw new IllegalArgumentException("El gol pertenece a un equipo que no juega el partido");
            }
        }
        return new Marcador(local, visitante);
    }
}