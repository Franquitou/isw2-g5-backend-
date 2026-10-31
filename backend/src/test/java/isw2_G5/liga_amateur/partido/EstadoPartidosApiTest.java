package isw2_G5.liga_amateur.partido;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import isw2_G5.liga_amateur.catalogo.entity.Equipo;
import isw2_G5.liga_amateur.catalogo.repository.EquipoRepository;
import isw2_G5.liga_amateur.partido.entity.Partido;
import isw2_G5.liga_amateur.partido.repository.PartidoRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba el cambio de estado de partidos (RF-07) con las reglas RN-05 y RN-07, contra una H2 en memoria
 * con {@code schema.sql} y {@code data.sql}. No toca {@code ./data/liga_amateur}.
 *
 * <p>Los partidos se crean directamente con el repositorio: las reglas de programación ya se prueban en
 * {@code ProgramacionPartidosApiTest}. Los cambios de estado se hacen siempre por el endpoint.</p>
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:estado-partidos-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always"
})
@AutoConfigureMockMvc
@Transactional
class EstadoPartidosApiTest {

    private static final LocalDateTime SABADO_18 = LocalDateTime.parse("2026-11-07T18:00");
    private static final String ADMIN = "ADMIN";
    private static final String DELEGADO = "DELEGADO";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private int norte;
    private int sur;
    private int centro;
    private int oeste;

    @BeforeEach
    void crearEquipos() {
        norte = crearEquipo(1, "Barrio Norte");
        sur = crearEquipo(1, "Barrio Sur");
        centro = crearEquipo(1, "Barrio Centro");
        oeste = crearEquipo(1, "Barrio Oeste");
    }

    // --- RN-05: transiciones permitidas ---

    @Test
    void unDelegadoIniciaUnPartidoProgramado() throws Exception {
        int id = crearPartido(1, 1, norte, sur);

        cambiarEstado(id, "EN_JUEGO", DELEGADO)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.estado").value("EN_JUEGO"))
                .andExpect(jsonPath("$.fechaHoraInicio").value("2026-11-07T18:00:00-03:00"));
    }

    @Test
    void unPartidoPuedeRecorrerProgramadoEnJuegoFinalizado() throws Exception {
        int id = crearPartido(1, 1, norte, sur);

        cambiarEstado(id, "EN_JUEGO", ADMIN).andExpect(status().isOk());
        cambiarEstado(id, "FINALIZADO", ADMIN)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));
    }

    @Test
    void unPartidoSePuedeSuspenderAntesODuranteElJuego() throws Exception {
        int programado = crearPartido(1, 1, norte, sur);
        int enJuego = crearPartido(1, 1, centro, oeste);
        cambiarEstado(enJuego, "EN_JUEGO", DELEGADO).andExpect(status().isOk());

        cambiarEstado(programado, "SUSPENDIDO", DELEGADO)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SUSPENDIDO"));
        cambiarEstado(enJuego, "SUSPENDIDO", DELEGADO)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SUSPENDIDO"));
    }

    // --- RN-05: transiciones prohibidas ---

    @Test
    void unaTransicionProhibidaDevuelve409() throws Exception {
        int id = crearPartido(1, 1, norte, sur);

        cambiarEstado(id, "FINALIZADO", ADMIN)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("No se puede pasar un partido de PROGRAMADO a FINALIZADO"));
        cambiarEstado(id, "PROGRAMADO", ADMIN)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No se puede pasar un partido de PROGRAMADO a PROGRAMADO"));
    }

    @Test
    void losEstadosFinalesNoAdmitenCambios() throws Exception {
        int finalizado = crearPartido(1, 1, norte, sur);
        cambiarEstado(finalizado, "EN_JUEGO", ADMIN).andExpect(status().isOk());
        cambiarEstado(finalizado, "FINALIZADO", ADMIN).andExpect(status().isOk());
        int suspendido = crearPartido(1, 1, centro, oeste);
        cambiarEstado(suspendido, "SUSPENDIDO", ADMIN).andExpect(status().isOk());

        cambiarEstado(finalizado, "EN_JUEGO", ADMIN)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No se puede pasar un partido de FINALIZADO a EN_JUEGO"));
        cambiarEstado(suspendido, "PROGRAMADO", ADMIN)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No se puede pasar un partido de SUSPENDIDO a PROGRAMADO"));
    }

    @Test
    void unPartidoIniciadoYaNoSePuedeReprogramarNiBorrar() throws Exception {
        int id = crearPartido(1, 1, norte, sur);
        cambiarEstado(id, "EN_JUEGO", DELEGADO).andExpect(status().isOk());

        mockMvc.perform(comoAdmin(put("/api/partidos/" + id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"torneoId":1,"numeroFecha":1,"equipoLocalId":%d,"equipoVisitanteId":%d,\
                                "canchaId":1,"arbitroId":1,"fechaHoraInicio":"2026-11-07T19:00:00-03:00"}"""
                                .formatted(norte, sur)))
                .andExpect(status().isConflict());
        mockMvc.perform(comoAdmin(delete("/api/partidos/" + id)))
                .andExpect(status().isConflict());
    }

    // --- Validación de entrada ---

    @Test
    void unEstadoInexistenteOAusenteDevuelve400() throws Exception {
        int id = crearPartido(1, 1, norte, sur);

        mockMvc.perform(comoAdmin(patch("/api/partidos/" + id + "/estado"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"JUGANDO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(comoAdmin(patch("/api/partidos/" + id + "/estado"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("estado"));
    }

    @Test
    void unPartidoInexistenteOUnIdInvalido() throws Exception {
        cambiarEstado(9999, "EN_JUEGO", ADMIN)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el partido con id 9999"));
        cambiarEstado(0, "EN_JUEGO", ADMIN)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El identificador de partido debe ser un entero positivo"));
    }

    // --- RN-07: orden de las jornadas ---

    @Test
    void noSeIniciaLaFechaSiguienteConPartidosProgramadosEnLaAnterior() throws Exception {
        crearPartido(1, 1, norte, sur);
        int fecha2 = crearPartido(1, 2, centro, oeste);

        cambiarEstado(fecha2, "EN_JUEGO", DELEGADO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("No se puede iniciar un partido de la fecha 2 mientras la fecha 1 tenga partidos sin terminar"));
    }

    @Test
    void noSeIniciaLaFechaSiguienteConPartidosEnJuegoEnLaAnterior() throws Exception {
        int fecha1 = crearPartido(1, 1, norte, sur);
        cambiarEstado(fecha1, "EN_JUEGO", DELEGADO).andExpect(status().isOk());
        int fecha2 = crearPartido(1, 2, centro, oeste);

        cambiarEstado(fecha2, "EN_JUEGO", DELEGADO)
                .andExpect(status().isConflict());
    }

    @Test
    void laFechaSiguienteSeIniciaSiLaAnteriorTerminoOFueSuspendida() throws Exception {
        int finalizado = crearPartido(1, 1, norte, sur);
        cambiarEstado(finalizado, "EN_JUEGO", ADMIN).andExpect(status().isOk());
        cambiarEstado(finalizado, "FINALIZADO", ADMIN).andExpect(status().isOk());
        int suspendido = crearPartido(1, 1, centro, oeste);
        cambiarEstado(suspendido, "SUSPENDIDO", ADMIN).andExpect(status().isOk());
        int fecha2 = crearPartido(1, 2, norte, centro);

        cambiarEstado(fecha2, "EN_JUEGO", DELEGADO)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_JUEGO"));
    }

    @Test
    void rn07SoloSeAplicaAlIniciar() throws Exception {
        crearPartido(1, 1, norte, sur);
        int fecha2 = crearPartido(1, 2, centro, oeste);

        cambiarEstado(fecha2, "SUSPENDIDO", DELEGADO)
                .andExpect(status().isOk());
    }

    @Test
    void unaJornadaAnteriorSinPartidosNoBloquea() throws Exception {
        int fecha2 = crearPartido(1, 2, norte, sur);

        cambiarEstado(fecha2, "EN_JUEGO", DELEGADO)
                .andExpect(status().isOk());
    }

    @Test
    void lasJornadasDeOtroTorneoNoBloquean() throws Exception {
        jdbcTemplate.update("INSERT INTO torneo (nombre, anio, activo) VALUES ('Clausura 2026', 2026, TRUE)");
        Integer clausura = jdbcTemplate.queryForObject(
                "SELECT id FROM torneo WHERE nombre = 'Clausura 2026'", Integer.class);
        crearPartido(clausura, 1, crearEquipo(clausura, "Ajeno A"), crearEquipo(clausura, "Ajeno B"));
        int fecha2 = crearPartido(1, 2, norte, sur);

        cambiarEstado(fecha2, "EN_JUEGO", DELEGADO)
                .andExpect(status().isOk());
    }

    // --- Permisos ---

    @Test
    void sinTokenDevuelve401() throws Exception {
        int id = crearPartido(1, 1, norte, sur);

        mockMvc.perform(patch("/api/partidos/" + id + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_JUEGO\"}"))
                .andExpect(status().isUnauthorized());
    }

    // --- Utilidades ---

    private int crearEquipo(int torneoId, String nombre) {
        return equipoRepository.save(new Equipo(torneoId, nombre, null)).getId();
    }

    /** Cada partido empieza en un día distinto, para no depender de las reglas de RF-06. */
    private int crearPartido(int torneoId, int numeroFecha, int local, int visitante) {
        LocalDateTime inicio = SABADO_18.plusDays(partidoRepository.count());
        return partidoRepository.save(new Partido(torneoId, numeroFecha, local, visitante, 1, 1, inicio)).getId();
    }

    private ResultActions cambiarEstado(int id, String estado, String rol) throws Exception {
        return mockMvc.perform(patch("/api/partidos/" + id + "/estado")
                .with(user(rol.toLowerCase()).roles(rol))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"" + estado + "\"}"));
    }

    private MockHttpServletRequestBuilder comoAdmin(MockHttpServletRequestBuilder builder) {
        return builder.with(user("admin").roles(ADMIN));
    }
}