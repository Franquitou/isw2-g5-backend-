package isw2_G5.liga_amateur.evento.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import isw2_G5.liga_amateur.evento.entity.EventoPartido;
import isw2_G5.liga_amateur.evento.entity.MotivoSuspension;
import isw2_G5.liga_amateur.evento.entity.TipoEvento;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Pruebas de la regla de suspensión (RF-12) sin Spring ni base de datos. */
class SuspensionCalculatorTest {

    @Test
    void sinEventosNadieQuedaSuspendido() {
        assertEquals(Map.of(), SuspensionCalculator.calcular(List.of()));
    }

    @Test
    void unaRojaSuspendePorRoja() {
        assertEquals(Map.of(5, MotivoSuspension.ROJA),
                SuspensionCalculator.calcular(List.of(evento(5, TipoEvento.ROJA, 30))));
    }

    @Test
    void dosAmarillasEnElMismoPartidoSuspendenPorDobleAmarilla() {
        assertEquals(Map.of(5, MotivoSuspension.DOBLE_AMARILLA),
                SuspensionCalculator.calcular(List.of(
                        evento(5, TipoEvento.AMARILLA, 10), evento(5, TipoEvento.AMARILLA, 60))));
    }

    @Test
    void unaSolaAmarillaNoSuspende() {
        assertEquals(Map.of(),
                SuspensionCalculator.calcular(List.of(evento(5, TipoEvento.AMARILLA, 10))));
    }

    @Test
    void rojaYDobleAmarillaSeInformaComoRojaSinImportarElOrden() {
        assertEquals(Map.of(5, MotivoSuspension.ROJA), SuspensionCalculator.calcular(List.of(
                evento(5, TipoEvento.AMARILLA, 10), evento(5, TipoEvento.AMARILLA, 20),
                evento(5, TipoEvento.ROJA, 30))));
        assertEquals(Map.of(5, MotivoSuspension.ROJA), SuspensionCalculator.calcular(List.of(
                evento(5, TipoEvento.ROJA, 5), evento(5, TipoEvento.AMARILLA, 10),
                evento(5, TipoEvento.AMARILLA, 20))));
    }

    @Test
    void losGolesNoCuentanParaLaSuspension() {
        assertEquals(Map.of(), SuspensionCalculator.calcular(List.of(
                evento(5, TipoEvento.GOL, 1), evento(5, TipoEvento.GOL, 2))));
    }

    @Test
    void cadaJugadorSeEvaluaPorSeparado() {
        Map<Integer, MotivoSuspension> esperado = Map.of(
                5, MotivoSuspension.ROJA, 6, MotivoSuspension.DOBLE_AMARILLA);

        assertEquals(esperado, SuspensionCalculator.calcular(List.of(
                evento(5, TipoEvento.ROJA, 1), evento(6, TipoEvento.AMARILLA, 2),
                evento(7, TipoEvento.AMARILLA, 3), evento(6, TipoEvento.AMARILLA, 4))));
    }

    @Test
    void unaListaNulaSeRechaza() {
        assertThrows(NullPointerException.class, () -> SuspensionCalculator.calcular(null));
    }

    private static EventoPartido evento(Integer jugadorId, TipoEvento tipo, int minuto) {
        return new EventoPartido(20, jugadorId, tipo, minuto);
    }
}
