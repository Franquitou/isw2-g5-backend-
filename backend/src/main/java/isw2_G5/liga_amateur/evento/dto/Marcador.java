package isw2_G5.liga_amateur.evento.dto;

/**
 * Resultado derivado de un partido (RN-06). Es el objeto {@code resultado} idéntico en el listado de José
 * y en el detalle público.
 *
 * @param golesLocal goles del equipo local
 * @param golesVisitante goles del equipo visitante
 */
public record Marcador(int golesLocal, int golesVisitante) {
}
