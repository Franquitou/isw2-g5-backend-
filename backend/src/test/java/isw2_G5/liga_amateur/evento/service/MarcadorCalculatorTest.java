package isw2_G5.liga_amateur.evento.service;

import static org.assertj.core.api.Assertions.assertThat;

import isw2_G5.liga_amateur.evento.dto.Marcador;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pruebas de la regla única del marcador (RN-06). */
class MarcadorCalculatorTest {

    @Test
    void sinGolesElMarcadorEsCeroACero() {
        assertThat(MarcadorCalculator.calcular(1, 2, List.of())).isEqualTo(new Marcador(0, 0));
    }

    @Test
    void cadaGolSeAsignaAlEquipoDelGoleador() {
        assertThat(MarcadorCalculator.calcular(1, 2, List.of(1, 1, 2))).isEqualTo(new Marcador(2, 1));
    }

    @Test
    void unGolDeUnEquipoAjenoAlPartidoSeIgnora() {
        assertThat(MarcadorCalculator.calcular(1, 2, List.of(9))).isEqualTo(new Marcador(0, 0));
    }
}
