package isw2_G5.liga_amateur.evento.dto;

/**
 * Resultado de un partido calculado a partir de sus eventos de gol (RN-06).
 * Se serializa como {@code resultado: {golesLocal, golesVisitante}} en el listado y en el detalle.
 *
 * @param golesLocal goles a favor del equipo local
 * @param golesVisitante goles a favor del equipo visitante
 */
public record Marcador(int golesLocal, int golesVisitante) {
}