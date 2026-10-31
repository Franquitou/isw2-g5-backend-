package isw2_G5.liga_amateur.evento.service;

import static org.assertj.core.api.Assertions.assertThat;

import isw2_G5.liga_amateur.evento.entity.MotivoSuspension;
import isw2_G5.liga_amateur.evento.entity.TipoEvento;
import isw2_G5.liga_amateur.evento.repository.TarjetaDeJugador;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Pruebas de las reglas de suspensión (RN-03/RN-04, RF-12). */
class SuspensionCalculatorTest {

    private static TarjetaDeJugador tarjeta(int partido, int jugador, int equipo, TipoEvento tipo) {
        return new TarjetaDeJugador(partido, jugador, equipo, tipo);
    }

    @Test
    void unaRojaSuspende() {
        var sanciones = SuspensionCalculator.calcular(List.of(tarjeta(10, 7, 2, TipoEvento.ROJA)));
        assertThat(sanciones).hasSize(1);
        assertThat(sanciones.get(0).motivo()).isEqualTo(MotivoSuspension.ROJA);
    }

    @Test
    void dosAmarillasEnElMismoPartidoSuspenden() {
        var sanciones = SuspensionCalculator.calcular(List.of(
                tarjeta(10, 5, 1, TipoEvento.AMARILLA), tarjeta(10, 5, 1, TipoEvento.AMARILLA)));
        assertThat(sanciones).hasSize(1);
        assertThat(sanciones.get(0).motivo()).isEqualTo(MotivoSuspension.DOBLE_AMARILLA);
    }

    @Test
    void unaSolaAmarillaNoSuspende() {
        assertThat(SuspensionCalculator.calcular(List.of(tarjeta(10, 6, 1, TipoEvento.AMARILLA)))).isEmpty();
    }

    @Test
    void lasAmarillasDePartidosDistintosNoSeAcumulan() {
        assertThat(SuspensionCalculator.calcular(List.of(
                tarjeta(10, 9, 3, TipoEvento.AMARILLA), tarjeta(11, 9, 3, TipoEvento.AMARILLA)))).isEmpty();
    }

    @Test
    void rojaMasDoblesAmarillasEnElMismoPartidoCuentaComoRoja() {
        var sanciones = SuspensionCalculator.calcular(List.of(
                tarjeta(10, 8, 2, TipoEvento.AMARILLA), tarjeta(10, 8, 2, TipoEvento.AMARILLA),
                tarjeta(10, 8, 2, TipoEvento.ROJA)));
        assertThat(sanciones).hasSize(1);
        assertThat(sanciones.get(0).motivo()).isEqualTo(MotivoSuspension.ROJA);
    }
}
