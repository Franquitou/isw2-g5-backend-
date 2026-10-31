package isw2_G5.liga_amateur.partido;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import isw2_G5.liga_amateur.catalogo.entity.Equipo;
import isw2_G5.liga_amateur.catalogo.entity.Jugador;
import isw2_G5.liga_amateur.catalogo.repository.EquipoRepository;
import isw2_G5.liga_amateur.catalogo.repository.JugadorRepository;
import isw2_G5.liga_amateur.evento.entity.EventoPartido;
import isw2_G5.liga_amateur.evento.entity.TipoEvento;
import isw2_G5.liga_amateur.evento.repository.EventoPartidoRepository;
import isw2_G5.liga_amateur.partido.entity.EstadoPartido;
import isw2_G5.liga_amateur.partido.entity.Partido;
import isw2_G5.liga_amateur.partido.repository.PartidoRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba el listado público por jornada (RF-10) contra una H2 en memoria con {@code schema.sql} y
 * {@code data.sql}. No toca {@code ./data/liga_amateur}.
 *
 * <p>Los partidos y eventos se crean con los repositorios: la programación y el registro de eventos ya se
 * prueban en sus propias clases. La consulta se hace siempre por el endpoint, sin token.</p>
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:listado-partidos-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always"
})
@AutoConfigureMockMvc
@Transactional
class ListadoPartidosApiTest {

    private static final LocalDateTime SABADO_18 = LocalDateTime.parse("2026-11-07T18:00");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private JugadorRepository jugadorRepository;
    @Autowired
    private PartidoRepository partidoRepository;
    @Autowired
    private EventoPartidoRepository eventoRepository;

    private Equipo norte;
    private Equipo sur;
    private Equipo centro;
    private Equipo oeste;

    @BeforeEach
    void crearEquipos() {
        norte = equipoRepository.save(new Equipo(1, "Barrio Norte", null));
        sur = equipoRepository.save(new Equipo(1, "Barrio Sur", null));
        centro = equipoRepository.save(new Equipo(1, "Barrio Centro", null));
        oeste = equipoRepository.save(new Equipo(1, "Barrio Oeste", null));
    }

    @Test
    void devuelveLaJornadaConResultadoDerivadoSinTokenYOrdenadaPorInicio() throws Exception {
        Partido primero = crearPartido(1, norte, sur, SABADO_18, EstadoPartido.FINALIZADO);
        Partido segundo = crearPartido(1, centro, oeste, SABADO_18.plusHours(2), EstadoPartido.EN_JUEGO);
        Jugador goleadorNorte = crearJugador(norte, "30111222");
        Jugador goleadorSur = crearJugador(sur, "30111333");
        Jugador goleadorCentro = crearJugador(centro, "30111444");
        crearGol(primero, goleadorNorte, 10);
        crearGol(primero, goleadorNorte, 55);
        crearGol(primero, goleadorSur, 70);
        crearGol(segundo, goleadorCentro, 15);

        mockMvc.perform(get("/api/torneos/1/partidos?numeroFecha=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(primero.getId()))
                .andExpect(jsonPath("$[0].estado").value("FINALIZADO"))
                .andExpect(jsonPath("$[0].fechaHoraInicio").value("2026-11-07T18:00:00-03:00"))
                .andExpect(jsonPath("$[0].resultado.golesLocal").value(2))
                .andExpect(jsonPath("$[0].resultado.golesVisitante").value(1))
                .andExpect(jsonPath("$[1].id").value(segundo.getId()))
                .andExpect(jsonPath("$[1].estado").value("EN_JUEGO"))
                .andExpect(jsonPath("$[1].fechaHoraInicio").value("2026-11-07T20:00:00-03:00"))
                .andExpect(jsonPath("$[1].resultado.golesLocal").value(1))
                .andExpect(jsonPath("$[1].resultado.golesVisitante").value(0));
    }

    @Test
    void devuelveListaVaciaSiLaJornadaNoTienePartidos() throws Exception {
        crearPartido(1, norte, sur, SABADO_18, EstadoPartido.PROGRAMADO);

        mockMvc.perform(get("/api/torneos/1/partidos?numeroFecha=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void sinGolesElResultadoEsCeroACero() throws Exception {
        Partido partido = crearPartido(1, norte, sur, SABADO_18, EstadoPartido.PROGRAMADO);

        mockMvc.perform(get("/api/torneos/1/partidos?numeroFecha=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(partido.getId()))
                .andExpect(jsonPath("$[0].resultado.golesLocal").value(0))
                .andExpect(jsonPath("$[0].resultado.golesVisitante").value(0));
    }

    @Test
    void validaElTorneoYLaJornada() throws Exception {
        mockMvc.perform(get("/api/torneos/1/partidos"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/torneos/1/partidos?numeroFecha=0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/torneos/0/partidos?numeroFecha=1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/torneos/9999/partidos?numeroFecha=1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el torneo con id 9999"));
    }

    private Partido crearPartido(int numeroFecha, Equipo local, Equipo visitante, LocalDateTime inicio,
            EstadoPartido estado) {
        Partido partido = new Partido(1, numeroFecha, local.getId(), visitante.getId(), 1, 1, inicio);
        ReflectionTestUtils.setField(partido, "estado", estado);
        return partidoRepository.save(partido);
    }

    private Jugador crearJugador(Equipo equipo, String dni) {
        return jugadorRepository.save(new Jugador(equipo.getId(), "Jugador", "Test", dni, null));
    }

    private void crearGol(Partido partido, Jugador jugador, int minuto) {
        eventoRepository.save(new EventoPartido(partido.getId(), jugador.getId(), TipoEvento.GOL, minuto));
    }
}
