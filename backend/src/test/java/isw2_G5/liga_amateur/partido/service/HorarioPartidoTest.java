package isw2_G5.liga_amateur.partido.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Prueba la zona horaria de la liga y el choque de horarios de RN-01/RN-02, sin Spring ni base de datos. */
class HorarioPartidoTest {

    private static final LocalDateTime QUINCE_HS = LocalDateTime.parse("2026-10-28T15:00");

    @ParameterizedTest(name = "partido a las 15:00 vs otro a las {0} -> chocan = {1}")
    @CsvSource({
            "2026-10-28T12:00, false",
            "2026-10-28T13:00, false",
            "2026-10-28T13:01, true",
            "2026-10-28T14:00, true",
            "2026-10-28T15:00, true",
            "2026-10-28T16:59, true",
            "2026-10-28T17:00, false",
            "2026-10-28T19:00, false"
    })
    void chocanSoloSiLosIniciosEstanAMenosDeDosHoras(LocalDateTime otroInicio, boolean esperado) {
        assertThat(HorarioPartido.chocan(QUINCE_HS, otroInicio)).isEqualTo(esperado);
    }

    @Test
    void elChoqueNoDependeDelOrdenDeLosPartidos() {
        LocalDateTime dieciseisHs = LocalDateTime.parse("2026-10-28T16:00");

        assertThat(HorarioPartido.chocan(QUINCE_HS, dieciseisHs)).isTrue();
        assertThat(HorarioPartido.chocan(dieciseisHs, QUINCE_HS)).isTrue();
    }

    @Test
    void consideraElCambioDeDia() {
        LocalDateTime casiMedianoche = LocalDateTime.parse("2026-10-28T23:30");
        LocalDateTime diaSiguiente = LocalDateTime.parse("2026-10-29T00:30");

        assertThat(HorarioPartido.chocan(casiMedianoche, diaSiguiente)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} en Santa Fe")
    @CsvSource({
            "2026-10-28T18:00:00-03:00, 2026-10-28T18:00",
            "2026-10-28T21:00:00Z,      2026-10-28T18:00",
            "2026-10-28T16:00:00-05:00, 2026-10-28T18:00",
            "2026-10-28T23:30:00-05:00, 2026-10-29T01:30"
    })
    void convierteCualquierOffsetALaHoraDeLaLiga(OffsetDateTime recibido, LocalDateTime esperado) {
        assertThat(HorarioPartido.aHoraLiga(recibido)).isEqualTo(esperado);
    }

    @Test
    void mismoInstanteConDistintoOffsetChoca() {
        LocalDateTime enArgentina = HorarioPartido.aHoraLiga(OffsetDateTime.parse("2026-10-28T18:00:00-03:00"));
        LocalDateTime enUtc = HorarioPartido.aHoraLiga(OffsetDateTime.parse("2026-10-28T21:00:00Z"));

        assertThat(HorarioPartido.chocan(enArgentina, enUtc)).isTrue();
    }

    @Test
    void respondeConElOffsetDeLaLiga() {
        OffsetDateTime respuesta = HorarioPartido.conZonaLiga(LocalDateTime.parse("2026-10-28T18:00"));

        assertThat(respuesta).isEqualTo(OffsetDateTime.parse("2026-10-28T18:00:00-03:00"));
        assertThat(respuesta.toString()).isEqualTo("2026-10-28T18:00-03:00");
    }

    @Test
    void laConversionEsReversible() {
        OffsetDateTime original = OffsetDateTime.parse("2026-10-28T18:00:00-03:00");

        assertThat(HorarioPartido.conZonaLiga(HorarioPartido.aHoraLiga(original))).isEqualTo(original);
    }

    @Test
    void rechazaValoresNulos() {
        assertThatThrownBy(() -> HorarioPartido.chocan(null, QUINCE_HS))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> HorarioPartido.aHoraLiga(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> HorarioPartido.conZonaLiga(null))
                .isInstanceOf(NullPointerException.class);
    }
}