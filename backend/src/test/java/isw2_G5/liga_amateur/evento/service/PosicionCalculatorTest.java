package isw2_G5.liga_amateur.evento.service;

import static org.assertj.core.api.Assertions.assertThat;

import isw2_G5.liga_amateur.evento.dto.PosicionResponse;
import isw2_G5.liga_amateur.evento.service.PosicionCalculator.ResultadoFinalizado;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Pruebas de la tabla de posiciones (RF-11). */
class PosicionCalculatorTest {

    private static final Map<Integer, String> EQUIPOS = Map.of(1, "Barrio Norte", 2, "Atlético", 3, "Zorros", 4, "Alba");

    @Test
    void sumaTresPuntosPorVictoriaUnoPorEmpateYCeroPorDerrota() {
        List<PosicionResponse> tabla = PosicionCalculator.calcular(EQUIPOS, List.of(
                new ResultadoFinalizado(1, 2, 2, 0), new ResultadoFinalizado(3, 4, 1, 1)));
        PosicionResponse primero = tabla.get(0);
        assertThat(primero.equipo()).isEqualTo("Barrio Norte");
        assertThat(primero).extracting(PosicionResponse::pj, PosicionResponse::pg, PosicionResponse::gf,
                PosicionResponse::gc, PosicionResponse::dg, PosicionResponse::pts).containsExactly(1, 1, 2, 0, 2, 3);
        PosicionResponse ultimo = tabla.get(3);
        assertThat(ultimo.equipo()).isEqualTo("Atlético");
        assertThat(ultimo).extracting(PosicionResponse::pp, PosicionResponse::dg, PosicionResponse::pts)
                .containsExactly(1, -2, 0);
    }

    @Test
    void incluyeEquiposSinPartidosConCeros() {
        List<PosicionResponse> tabla = PosicionCalculator.calcular(EQUIPOS, List.of());
        assertThat(tabla).hasSize(4).allSatisfy(f -> assertThat(f.pts()).isZero());
        assertThat(tabla).extracting(PosicionResponse::equipo)
                .containsExactly("Alba", "Atlético", "Barrio Norte", "Zorros");
    }

    @Test
    void desempataPorDiferenciaDeGolLuegoGolesAFavorYNombre() {
        // Alba y Zorros: 1 punto, DG 0; luego mismos GF, desempata el nombre.
        List<PosicionResponse> tabla = PosicionCalculator.calcular(EQUIPOS, List.of(
                new ResultadoFinalizado(3, 4, 1, 1)));
        assertThat(tabla).extracting(PosicionResponse::equipo).containsExactly("Alba", "Zorros", "Atlético",
                "Barrio Norte");
        // Con más goles a favor a igual DG, gana el que más convirtió.
        List<PosicionResponse> porGf = PosicionCalculator.calcular(EQUIPOS, List.of(
                new ResultadoFinalizado(3, 4, 3, 3)));
        assertThat(porGf.get(0).equipo()).isEqualTo("Alba");
    }
}
