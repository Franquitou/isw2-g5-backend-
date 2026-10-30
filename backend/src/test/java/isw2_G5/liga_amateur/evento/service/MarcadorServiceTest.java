package isw2_G5.liga_amateur.evento.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import isw2_G5.liga_amateur.catalogo.entity.Jugador;
import isw2_G5.liga_amateur.catalogo.repository.JugadorRepository;
import isw2_G5.liga_amateur.evento.dto.Marcador;
import isw2_G5.liga_amateur.evento.entity.EventoPartido;
import isw2_G5.liga_amateur.evento.entity.TipoEvento;
import isw2_G5.liga_amateur.evento.repository.EventoPartidoRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Pruebas de RN-06 con repositorios simulados: no usan Spring ni la base de datos. */
class MarcadorServiceTest {

    private static final Integer PARTIDO = 20;
    private static final Integer LOCAL = 7;
    private static final Integer VISITANTE = 8;

    private EventoPartidoRepository eventos;
    private JugadorRepository jugadores;
    private MarcadorService service;

    @BeforeEach
    void setUp() {
        eventos = mock(EventoPartidoRepository.class);
        jugadores = mock(JugadorRepository.class);
        service = new MarcadorService(eventos, jugadores);
    }

    @Test
    void sinGolesDevuelveCeroACeroSinConsultarJugadores() {
        when(eventos.findByPartidoIdAndTipo(PARTIDO, TipoEvento.GOL)).thenReturn(List.of());

        assertEquals(new Marcador(0, 0), service.calcular(PARTIDO, LOCAL, VISITANTE));

        verify(jugadores, never()).findAllById(any());
    }

    @Test
    void cadaGolSumaAlEquipoDelJugador() {
        Jugador delLocal = jugador(15, LOCAL);
        Jugador delVisitante = jugador(16, VISITANTE);
        when(eventos.findByPartidoIdAndTipo(PARTIDO, TipoEvento.GOL)).thenReturn(List.of(
                gol(15, 10), gol(16, 40), gol(15, 80)));
        when(jugadores.findAllById(any())).thenReturn(List.of(delLocal, delVisitante));

        assertEquals(new Marcador(2, 1), service.calcular(PARTIDO, LOCAL, VISITANTE));
    }

    @Test
    void golDeUnJugadorDeOtroEquipoEsError() {
        Jugador ajeno = jugador(99, 50);
        when(eventos.findByPartidoIdAndTipo(PARTIDO, TipoEvento.GOL)).thenReturn(List.of(gol(99, 30)));
        when(jugadores.findAllById(any())).thenReturn(List.of(ajeno));

        assertThrows(IllegalArgumentException.class, () -> service.calcular(PARTIDO, LOCAL, VISITANTE));
    }

    private static EventoPartido gol(Integer jugadorId, int minuto) {
        return new EventoPartido(PARTIDO, jugadorId, TipoEvento.GOL, minuto);
    }

    private static Jugador jugador(Integer id, Integer equipoId) {
        Jugador jugador = new Jugador(equipoId, "Nombre", "Apellido", "dni-" + id, null);
        ReflectionTestUtils.setField(jugador, "id", id);
        return jugador;
    }
}
