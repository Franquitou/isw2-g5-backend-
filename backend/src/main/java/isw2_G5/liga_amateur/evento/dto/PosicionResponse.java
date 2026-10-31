package isw2_G5.liga_amateur.evento.dto;

/**
 * Fila de la tabla de posiciones (RF-11).
 *
 * @param equipoId equipo
 * @param equipo nombre del equipo
 * @param pj partidos jugados
 * @param pg ganados
 * @param pe empatados
 * @param pp perdidos
 * @param gf goles a favor
 * @param gc goles en contra
 * @param dg diferencia de gol
 * @param pts puntos
 */
public record PosicionResponse(Integer equipoId, String equipo, int pj, int pg, int pe, int pp,
        int gf, int gc, int dg, int pts) {
}
