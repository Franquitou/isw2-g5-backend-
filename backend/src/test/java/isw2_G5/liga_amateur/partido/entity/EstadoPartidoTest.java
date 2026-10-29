package isw2_G5.liga_amateur.partido.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/** Prueba la máquina de estados de RN-05 sin Spring ni base de datos. */
class EstadoPartidoTest {

    /** Única lista de transiciones válidas, escrita a mano a partir de RN-05 y docs/API.md §6. */
    private static final Set<String> PERMITIDAS = Set.of(
            "PROGRAMADO>EN_JUEGO",
            "PROGRAMADO>SUSPENDIDO",
            "EN_JUEGO>FINALIZADO",
            "EN_JUEGO>SUSPENDIDO");

    static Stream<Arguments> todasLasCombinaciones() {
        return Arrays.stream(EstadoPartido.values())
                .flatMap(origen -> Arrays.stream(EstadoPartido.values())
                        .map(destino -> Arguments.of(origen, destino)));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("todasLasCombinaciones")
    void soloPermiteLasTransicionesDeRn05(EstadoPartido origen, EstadoPartido destino) {
        boolean esperado = PERMITIDAS.contains(origen + ">" + destino);

        assertThat(origen.puedeCambiarA(destino)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPartido.class, names = {"FINALIZADO", "SUSPENDIDO"})
    void losEstadosFinalesNoAdmitenNingunaTransicion(EstadoPartido estado) {
        assertThat(estado.esFinal()).isTrue();
        assertThat(EstadoPartido.values()).noneMatch(estado::puedeCambiarA);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPartido.class, names = {"PROGRAMADO", "EN_JUEGO"})
    void losEstadosNoFinalesTienenAlgunaSalida(EstadoPartido estado) {
        assertThat(estado.esFinal()).isFalse();
        assertThat(EstadoPartido.values()).anyMatch(estado::puedeCambiarA);
    }

    @Test
    void ningunEstadoPuedeQuedarseEnElMismoEstado() {
        assertThat(EstadoPartido.values()).noneMatch(estado -> estado.puedeCambiarA(estado));
    }

    @Test
    void losNombresCoincidenConElCheckDelSchema() {
        assertThat(Arrays.stream(EstadoPartido.values()).map(Enum::name))
                .containsExactly("PROGRAMADO", "EN_JUEGO", "FINALIZADO", "SUSPENDIDO");
    }

    @Test
    void rechazaUnDestinoNulo() {
        assertThatThrownBy(() -> EstadoPartido.PROGRAMADO.puedeCambiarA(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("El estado de destino es obligatorio");
    }
}