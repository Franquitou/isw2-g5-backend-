package isw2_G5.liga_amateur.evento.service;

import isw2_G5.liga_amateur.evento.dto.PosicionResponse;
import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Regla pura de la tabla de posiciones (RF-11): victoria 3 puntos, empate 1, derrota 0; orden por puntos,
 * diferencia de gol, goles a favor y nombre alfabético.
 */
public final class PosicionCalculator {

    /**
     * Resultado de un partido FINALIZADO.
     *
     * @param equipoLocalId equipo local
     * @param equipoVisitanteId equipo visitante
     * @param golesLocal goles del local
     * @param golesVisitante goles del visitante
     */
    public record ResultadoFinalizado(Integer equipoLocalId, Integer equipoVisitanteId,
            int golesLocal, int golesVisitante) {
    }

    private PosicionCalculator() {
    }

    /**
     * Calcula la tabla.
     *
     * @param equipos nombre de cada equipo por id; todos aparecen en la tabla, incluso sin partidos
     * @param resultados partidos finalizados del torneo
     * @return filas ordenadas según RF-11
     */
    public static List<PosicionResponse> calcular(Map<Integer, String> equipos,
            Collection<ResultadoFinalizado> resultados) {
        Map<Integer, int[]> filas = new HashMap<>(); // {pj, pg, pe, pp, gf, gc}
        equipos.keySet().forEach(id -> filas.put(id, new int[6]));
        for (ResultadoFinalizado r : resultados) {
            acumular(filas.computeIfAbsent(r.equipoLocalId(), k -> new int[6]), r.golesLocal(), r.golesVisitante());
            acumular(filas.computeIfAbsent(r.equipoVisitanteId(), k -> new int[6]), r.golesVisitante(),
                    r.golesLocal());
        }
        Collator collator = Collator.getInstance(Locale.forLanguageTag("es-AR"));
        return filas.entrySet().stream()
                .map(e -> fila(e.getKey(), equipos.getOrDefault(e.getKey(), ""), e.getValue()))
                .sorted(Comparator.comparingInt(PosicionResponse::pts).reversed()
                        .thenComparing(Comparator.comparingInt(PosicionResponse::dg).reversed())
                        .thenComparing(Comparator.comparingInt(PosicionResponse::gf).reversed())
                        .thenComparing(PosicionResponse::equipo, collator)
                        .thenComparing(PosicionResponse::equipoId))
                .toList();
    }

    private static void acumular(int[] fila, int golesFavor, int golesContra) {
        fila[0]++;
        if (golesFavor > golesContra) {
            fila[1]++;
        } else if (golesFavor == golesContra) {
            fila[2]++;
        } else {
            fila[3]++;
        }
        fila[4] += golesFavor;
        fila[5] += golesContra;
    }

    private static PosicionResponse fila(Integer equipoId, String nombre, int[] f) {
        return new PosicionResponse(equipoId, nombre, f[0], f[1], f[2], f[3], f[4], f[5], f[4] - f[5],
                f[1] * 3 + f[2]);
    }
}
