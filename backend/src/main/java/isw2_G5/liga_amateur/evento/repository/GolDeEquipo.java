package isw2_G5.liga_amateur.evento.repository;

/**
 * Proyección de un gol: partido y equipo del jugador que lo marcó (no hay goles en contra).
 *
 * @param partidoId partido del gol
 * @param equipoId equipo del goleador
 */
public record GolDeEquipo(Integer partidoId, Integer equipoId) {
}
