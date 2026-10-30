package isw2_G5.liga_amateur.evento;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import isw2_G5.liga_amateur.catalogo.entity.Equipo;
import isw2_G5.liga_amateur.catalogo.entity.Jugador;
import isw2_G5.liga_amateur.catalogo.repository.EquipoRepository;
import isw2_G5.liga_amateur.catalogo.repository.JugadorRepository;
import isw2_G5.liga_amateur.evento.entity.EventoPartido;
import isw2_G5.liga_amateur.evento.entity.TipoEvento;
import isw2_G5.liga_amateur.evento.repository.EventoPartidoRepository;
import isw2_G5.liga_amateur.evento.repository.PartidoLecturaRepository;
import isw2_G5.liga_amateur.partido.entity.EstadoPartido;
import isw2_G5.liga_amateur.partido.entity.Partido;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba de punta a punta de eventos, detalle, tabla y suspendidos (RF-08 a RF-12) con H2 en memoria y los
 * datos de {@code schema.sql}/{@code data.sql} (torneo 1, cancha 1, árbitro 1). Los permisos se simulan con
 * usuarios de Spring Security Test.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:eventos-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always"
})
@AutoConfigureMockMvc
@Transactional
class EventosApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private JugadorRepository jugadorRepository;
    @Autowired
    private PartidoLecturaRepository partidoRepository;
    @Autowired
    private EventoPartidoRepository eventoRepository;

    private Equipo local;
    private Equipo visitante;
    private Jugador jugadorLocal;
    private Jugador jugadorVisitante;
    private Jugador jugadorAjeno;

    @BeforeEach
    void datos() {
        local = equipoRepository.save(new Equipo(1, "Barrio Norte", null));
        visitante = equipoRepository.save(new Equipo(1, "Atlético", null));
        Equipo otro = equipoRepository.save(new Equipo(1, "Zorros", null));
        jugadorLocal = jugadorRepository.save(new Jugador(local.getId(), "Juan", "Pérez", "30111222", null));
        jugadorVisitante = jugadorRepository.save(new Jugador(visitante.getId(), "Luis", "Gómez", "30111333", 9));
        jugadorAjeno = jugadorRepository.save(new Jugador(otro.getId(), "Ana", "Ruiz", "30111444", null));
    }

    private Partido partido(int fecha, EstadoPartido estado) {
        return partidoRepository.save(new Partido(1, fecha, local.getId(), visitante.getId(), 1, 1,
                LocalDateTime.of(2026, 10, 28, 18, 0), estado));
    }

    private MockHttpServletRequestBuilder comoDelegado(MockHttpServletRequestBuilder b) {
        return b.with(user("delegado").roles("DELEGADO")).contentType(MediaType.APPLICATION_JSON);
    }

    private static String evento(Integer jugadorId, String tipo, Object minuto) {
        return "{\"jugadorId\":" + jugadorId + ",\"tipo\":\"" + tipo + "\",\"minuto\":" + minuto + "}";
    }

    // --- RF-08 ---

    @Test
    void elDelegadoRegistraUnGolYElDetalleMuestraElResultado() throws Exception {
        Partido p = partido(1, EstadoPartido.EN_JUEGO);
        mockMvc.perform(comoDelegado(post("/api/partidos/" + p.getId() + "/eventos"))
                        .content(evento(jugadorLocal.getId(), "GOL", 35)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partidoId").value(p.getId()))
                .andExpect(jsonPath("$.tipo").value("GOL"))
                .andExpect(jsonPath("$.minuto").value(35));
        mockMvc.perform(get("/api/partidos/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado.golesLocal").value(1))
                .andExpect(jsonPath("$.resultado.golesVisitante").value(0))
                .andExpect(jsonPath("$.eventos", hasSize(1)))
                .andExpect(jsonPath("$.estado").value("EN_JUEGO"));
    }

    @Test
    void sinTokenElRegistroDeEventosDevuelve401() throws Exception {
        Partido p = partido(1, EstadoPartido.EN_JUEGO);
        mockMvc.perform(post("/api/partidos/" + p.getId() + "/eventos").contentType(MediaType.APPLICATION_JSON)
                        .content(evento(jugadorLocal.getId(), "GOL", 10)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void minutoFueraDeRangoOTipoInvalidoDevuelve400() throws Exception {
        Partido p = partido(1, EstadoPartido.EN_JUEGO);
        mockMvc.perform(comoDelegado(post("/api/partidos/" + p.getId() + "/eventos"))
                        .content(evento(jugadorLocal.getId(), "GOL", 0)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(comoDelegado(post("/api/partidos/" + p.getId() + "/eventos"))
                        .content(evento(jugadorLocal.getId(), "GOL", 121)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(comoDelegado(post("/api/partidos/" + p.getId() + "/eventos"))
                        .content(evento(jugadorLocal.getId(), "PENAL", 10)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void partidoOJugadorInexistenteDevuelve404() throws Exception {
        Partido p = partido(1, EstadoPartido.EN_JUEGO);
        mockMvc.perform(comoDelegado(post("/api/partidos/9999/eventos"))
                        .content(evento(jugadorLocal.getId(), "GOL", 10)))
                .andExpect(status().isNotFound());
        mockMvc.perform(comoDelegado(post("/api/partidos/" + p.getId() + "/eventos"))
                        .content(evento(9999, "GOL", 10)))
                .andExpect(status().isNotFound());
    }

    @Test
    void soloSeRegistraEnUnPartidoEnJuego() throws Exception {
        Partido p = partido(1, EstadoPartido.PROGRAMADO);
        mockMvc.perform(comoDelegado(post("/api/partidos/" + p.getId() + "/eventos"))
                        .content(evento(jugadorLocal.getId(), "GOL", 10)))
                .andExpect(status().isConflict());
    }

    @Test
    void unJugadorDeOtroEquipoOInactivoDevuelve409() throws Exception {
        Partido p = partido(1, EstadoPartido.EN_JUEGO);
        mockMvc.perform(comoDelegado(post("/api/partidos/" + p.getId() + "/eventos"))
                        .content(evento(jugadorAjeno.getId(), "GOL", 10)))
                .andExpect(status().isConflict());
        Jugador inactivo = jugadorRepository.save(new Jugador(local.getId(), "Pablo", "Díaz", "30111555", null));
        inactivo.darDeBaja();
        jugadorRepository.save(inactivo);
        mockMvc.perform(comoDelegado(post("/api/partidos/" + p.getId() + "/eventos"))
                        .content(evento(inactivo.getId(), "GOL", 10)))
                .andExpect(status().isConflict());
    }

    // --- RF-09 ---

    @Test
    void corregirUnEventoLoEliminaYRecalculaElMarcador() throws Exception {
        Partido p = partido(1, EstadoPartido.EN_JUEGO);
        EventoPartido gol = eventoRepository.save(new EventoPartido(p.getId(), jugadorLocal.getId(), TipoEvento.GOL, 20));
        mockMvc.perform(comoDelegado(delete("/api/partidos/" + p.getId() + "/eventos/" + gol.getId())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/partidos/" + p.getId()))
                .andExpect(jsonPath("$.resultado.golesLocal").value(0))
                .andExpect(jsonPath("$.eventos", hasSize(0)));
    }

    @Test
    void noSeBorraUnEventoAjenoNiFueraDeJuego() throws Exception {
        Partido enJuego = partido(1, EstadoPartido.EN_JUEGO);
        Partido finalizado = partido(2, EstadoPartido.FINALIZADO);
        EventoPartido deFinalizado = eventoRepository.save(
                new EventoPartido(finalizado.getId(), jugadorLocal.getId(), TipoEvento.GOL, 20));
        mockMvc.perform(comoDelegado(delete("/api/partidos/" + enJuego.getId() + "/eventos/" + deFinalizado.getId())))
                .andExpect(status().isNotFound());
        mockMvc.perform(comoDelegado(delete("/api/partidos/" + finalizado.getId() + "/eventos/" + deFinalizado.getId())))
                .andExpect(status().isConflict());
    }

    // --- RF-10 ---

    @Test
    void elDetalleDeUnPartidoInexistenteOInvalidoDevuelve404Y400() throws Exception {
        mockMvc.perform(get("/api/partidos/9999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/partidos/0")).andExpect(status().isBadRequest());
    }

    // --- RF-11 ---

    @Test
    void laTablaCuentaSoloPartidosFinalizadosEIncluyeEquiposSinPartidos() throws Exception {
        Partido p = partido(1, EstadoPartido.FINALIZADO);
        eventoRepository.save(new EventoPartido(p.getId(), jugadorLocal.getId(), TipoEvento.GOL, 10));
        eventoRepository.save(new EventoPartido(p.getId(), jugadorLocal.getId(), TipoEvento.GOL, 50));
        partido(2, EstadoPartido.EN_JUEGO); // no suma
        mockMvc.perform(get("/api/torneos/1/tabla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].equipo").value("Barrio Norte"))
                .andExpect(jsonPath("$[0].pj").value(1))
                .andExpect(jsonPath("$[0].pts").value(3))
                .andExpect(jsonPath("$[0].dg").value(2))
                .andExpect(jsonPath("$[2].equipo").value("Atlético"))
                .andExpect(jsonPath("$[2].pts").value(0));
        mockMvc.perform(get("/api/torneos/9999/tabla")).andExpect(status().isNotFound());
    }

    // --- RF-12 / RN-03 / RN-04 ---

    @Test
    void unaRojaSuspendeParaLaFechaSiguienteYBloqueaSusEventos() throws Exception {
        Partido anterior = partido(1, EstadoPartido.FINALIZADO);
        eventoRepository.save(new EventoPartido(anterior.getId(), jugadorVisitante.getId(), TipoEvento.ROJA, 80));
        Partido siguiente = partido(2, EstadoPartido.EN_JUEGO);

        mockMvc.perform(get("/api/torneos/1/suspendidos?numeroFecha=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].jugadorId").value(jugadorVisitante.getId()))
                .andExpect(jsonPath("$[0].equipoId").value(visitante.getId()))
                .andExpect(jsonPath("$[0].numeroFecha").value(2))
                .andExpect(jsonPath("$[0].motivo").value("ROJA"))
                .andExpect(jsonPath("$[0].numeroFechaSancion").value(1));
        mockMvc.perform(get("/api/torneos/1/suspendidos?numeroFecha=1"))
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(comoDelegado(post("/api/partidos/" + siguiente.getId() + "/eventos"))
                        .content(evento(jugadorVisitante.getId(), "GOL", 10)))
                .andExpect(status().isConflict());
    }

    @Test
    void laConsultaDeSuspendidosValidaSusParametros() throws Exception {
        mockMvc.perform(get("/api/torneos/1/suspendidos")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/torneos/1/suspendidos?numeroFecha=0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/torneos/9999/suspendidos?numeroFecha=2")).andExpect(status().isNotFound());
    }
}
