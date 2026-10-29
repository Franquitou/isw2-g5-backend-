package isw2_G5.liga_amateur.evento.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import isw2_G5.liga_amateur.evento.dto.Marcador;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pruebas de RN-06 sin Spring ni H2: no tocan la base de datos. */
class MarcadorCalculatorTest {

    private static final Integer LOCAL = 7;
    private static final Integer VISITANTE = 8;

    @Test
    void sinGolesDevuelveCeroACero() {
        assertEquals(new Marcador(0, 0), MarcadorCalculator.calcular(LOCAL, VISITANTE, List.of()));
    }

    @Test
    void cadaGolSumaAlEquipoDelJugador() {
        Marcador marcador = MarcadorCalculator.calcular(LOCAL, VISITANTE, List.of(LOCAL, VISITANTE, LOCAL));
        assertEquals(new Marcador(2, 1), marcador);
    }

    @Test
    void soloGolesDelVisitante() {
        Marcador marcador = MarcadorCalculator.calcular(LOCAL, VISITANTE, List.of(VISITANTE, VISITANTE));
        assertEquals(new Marcador(0, 2), marcador);
    }

    @Test
    void golDeUnEquipoAjenoAlPartidoEsError() {
        assertThrows(IllegalArgumentException.class,
                () -> MarcadorCalculator.calcular(LOCAL, VISITANTE, List.of(LOCAL, 99)));
    }

    @Test
    void argumentosNulosSeRechazan() {
        assertThrows(NullPointerException.class, () -> MarcadorCalculator.calcular(null, VISITANTE, List.of()));
        assertThrows(NullPointerException.class, () -> MarcadorCalculator.calcular(LOCAL, VISITANTE, null));
    }
}