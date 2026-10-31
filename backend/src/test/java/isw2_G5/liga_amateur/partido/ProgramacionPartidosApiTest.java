package isw2_G5.liga_amateur.partido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import isw2_G5.liga_amateur.catalogo.entity.Arbitro;
import isw2_G5.liga_amateur.catalogo.entity.Cancha;
import isw2_G5.liga_amateur.catalogo.entity.Equipo;
import isw2_G5.liga_amateur.catalogo.repository.ArbitroRepository;
import isw2_G5.liga_amateur.catalogo.repository.CanchaRepository;
import isw2_G5.liga_amateur.catalogo.repository.EquipoRepository;
import isw2_G5.liga_amateur.partido.repository.PartidoRepository;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
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
 * Prueba la programación de partidos (RF-06, RN-01 y RN-02) contra una H2 en memoria con
 * {@code schema.sql} y {@code data.sql}. No toca {@code ./data/liga_amateur}.
 * {@code ddl-auto=validate} hace fallar el arranque si la entidad {@code Partido} no coincide con la tabla.
 *
 * <p>Datos precargados usados: torneo 1, canchas 1 y 2, árbitros 1 y 2. Los equipos se crean en cada prueba.</p>
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:programacion-partidos-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always"
})
@AutoConfigureMockMvc
@Transactional
class ProgramacionPartidosApiTest {

    private static final String SABADO_18 = "2026-11-07T18:00:00-03:00";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private CanchaRepository canchaRepository;

    @Autowired
    private ArbitroRepository arbitroRepository;

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private int norte;
    private int sur;
    private int centro;
    private int oeste;

    @BeforeEach
    void crearEquipos() {
        norte = crearEquipo("Barrio Norte");
        sur = crearEquipo("Barrio Sur");
        centro = crearEquipo("Barrio Centro");
        oeste = crearEquipo("Barrio Oeste");
    }

    // --- Alta (RF-06) ---

    @Test
    void programarDevuelve201YElPartidoNaceProgramado() throws Exception {
        programar(cuerpo(1, norte, sur, 1, 1, SABADO_18))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.torneoId").value(1))
                .andExpect(jsonPath("$.numeroFecha").value(1))
                .andExpect(jsonPath("$.equipoLocalId").value(norte))
                .andExpect(jsonPath("$.equipoVisitanteId").value(sur))
                .andExpect(jsonPath("$.canchaId").value(1))
                .andExpect(jsonPath("$.arbitroId").value(1))
                .andExpect(jsonPath("$.fechaHoraInicio").value(SABADO_18))
                .andExpect(jsonPath("$.estado").value("PROGRAMADO"));
    }

    @Test
    void unaHoraEnUtcSeGuardaYRespondeEnLaHoraDeLaLiga() throws Exception {
        programar(cuerpo(1, norte, sur, 1, 1, "2026-11-07T21:00:00Z"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fechaHoraInicio").value(SABADO_18));
    }

    // --- Validación de entrada (400) ---

    @Test
    void unCuerpoVacioDevuelve400ConTodosLosCamposObligatorios() throws Exception {
        programar("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors", hasSize(7)));
    }

    @Test
    void unaJornadaMenorAUnoDevuelve400() throws Exception {
        programar(cuerpo(0, norte, sur, 1, 1, SABADO_18))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("numeroFecha"));
    }

    @Test
    void unaFechaSinOffsetDevuelve400() throws Exception {
        programar(cuerpo(1, norte, sur, 1, 1, "2026-11-07T18:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // --- Referencias inexistentes (404) ---

    @Test
    void lasReferenciasInexistentesDevuelven404() throws Exception {
        programar(cuerpoCon(9999, 1, norte, sur, 1, 1, SABADO_18))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el torneo con id 9999"));
        programar(cuerpo(1, norte, 9999, 1, 1, SABADO_18))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el equipo con id 9999"));
        programar(cuerpo(1, norte, sur, 9999, 1, SABADO_18))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe la cancha con id 9999"));
        programar(cuerpo(1, norte, sur, 1, 9999, SABADO_18))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el árbitro con id 9999"));
    }

    // --- Reglas de equipos, cancha y árbitro (409) ---

    @Test
    void elMismoEquipoComoLocalYVisitanteDevuelve409() throws Exception {
        programar(cuerpo(1, norte, norte, 1, 1, SABADO_18))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("El equipo local y el visitante deben ser distintos"));
    }

    @Test
    void unEquipoDeOtroTorneoDevuelve409() throws Exception {
        jdbcTemplate.update("INSERT INTO torneo (nombre, anio, activo) VALUES ('Clausura 2026', 2026, TRUE)");
        Integer clausura = jdbcTemplate.queryForObject(
                "SELECT id FROM torneo WHERE nombre = 'Clausura 2026'", Integer.class);
        int ajeno = equipoRepository.save(new Equipo(clausura, "Equipo Ajeno", null)).getId();

        programar(cuerpo(1, norte, ajeno, 1, 1, SABADO_18))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ambos equipos deben pertenecer al torneo del partido"));
    }

    @Test
    void unEquipoDadoDeBajaDevuelve409() throws Exception {
        equipoRepository.findById(sur).orElseThrow().darDeBaja();

        programar(cuerpo(1, norte, sur, 1, 1, SABADO_18))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El equipo visitante está dado de baja"));
    }

    @Test
    void unaCanchaOUnArbitroInhabilitadosDevuelven409() throws Exception {
        int canchaCerrada = canchaRepository.save(new Cancha("Cancha Cerrada", null, false)).getId();
        int arbitroInactivo = arbitroRepository.save(new Arbitro("Ana", "Acosta", "40111222", false)).getId();

        programar(cuerpo(1, norte, sur, canchaCerrada, 1, SABADO_18))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La cancha no está habilitada"));
        programar(cuerpo(1, norte, sur, 1, arbitroInactivo, SABADO_18))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El árbitro no está habilitado"));
    }

    @Test
    void unEquipoNoJuegaDosVecesEnLaMismaJornada() throws Exception {
        programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));

        programar(cuerpo(1, centro, sur, 2, 2, "2026-11-08T18:00:00-03:00"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Alguno de los equipos ya tiene un partido en la fecha 1"));
    }

    @Test
    void unEquipoPuedeJugarOtraJornada() throws Exception {
        programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));

        programar(cuerpo(2, sur, norte, 1, 1, "2026-11-14T18:00:00-03:00"))
                .andExpect(status().isCreated());
    }

    @Test
    void unPartidoSuspendidoNoOcupaLaJornadaDeSusEquipos() throws Exception {
        int suspendido = programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));
        cambiarEstadoEnBase(suspendido, "SUSPENDIDO");

        programar(cuerpo(1, norte, sur, 1, 1, "2026-11-08T18:00:00-03:00"))
                .andExpect(status().isCreated());
    }

    // --- Choque de horarios (RN-01 y RN-02) ---

    @Test
    void laCanchaOcupadaAMenosDeDosHorasDevuelve409() throws Exception {
        programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));

        programar(cuerpo(1, centro, oeste, 1, 2, "2026-11-07T19:59:00-03:00"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("La cancha ya tiene otro partido a menos de dos horas de ese horario"));
    }

    @Test
    void dosHorasExactasDeSeparacionEsValido() throws Exception {
        programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));

        programar(cuerpo(1, centro, oeste, 1, 1, "2026-11-07T20:00:00-03:00"))
                .andExpect(status().isCreated());
        programar(cuerpo(2, centro, oeste, 1, 1, "2026-11-07T16:00:00-03:00"))
                .andExpect(status().isCreated());
    }

    @Test
    void elArbitroOcupadoAMenosDeDosHorasDevuelve409() throws Exception {
        programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));

        programar(cuerpo(1, centro, oeste, 2, 1, "2026-11-07T17:00:00-03:00"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("El árbitro ya tiene otro partido a menos de dos horas de ese horario"));
    }

    @Test
    void elChoqueSeDetectaAunqueLaHoraLlegueConOtroOffset() throws Exception {
        programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));

        programar(cuerpo(1, centro, oeste, 1, 2, "2026-11-07T21:30:00Z"))
                .andExpect(status().isConflict());
    }

    @Test
    void unPartidoSuspendidoNoOcupaCanchaNiArbitro() throws Exception {
        int suspendido = programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));
        cambiarEstadoEnBase(suspendido, "SUSPENDIDO");

        programar(cuerpo(1, centro, oeste, 1, 1, SABADO_18))
                .andExpect(status().isCreated());
    }

    // --- Reprogramación (PUT) ---

    @Test
    void reprogramarDevuelve200YNoChocaConsigoMismo() throws Exception {
        int id = programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));

        mockMvc.perform(comoAdmin(put("/api/partidos/" + id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, norte, sur, 1, 1, "2026-11-07T18:30:00-03:00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.fechaHoraInicio").value("2026-11-07T18:30:00-03:00"))
                .andExpect(jsonPath("$.estado").value("PROGRAMADO"));
    }

    @Test
    void reprogramarRespetaLosChoquesConOtrosPartidos() throws Exception {
        programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));
        int otro = programarOk(cuerpo(1, centro, oeste, 2, 2, SABADO_18));

        mockMvc.perform(comoAdmin(put("/api/partidos/" + otro))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, centro, oeste, 1, 2, SABADO_18)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("La cancha ya tiene otro partido a menos de dos horas de ese horario"));
    }

    @Test
    void reprogramarConservaUnEquipoQueSeDioDeBajaDespues() throws Exception {
        int id = programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));
        equipoRepository.findById(sur).orElseThrow().darDeBaja();

        mockMvc.perform(comoAdmin(put("/api/partidos/" + id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, norte, sur, 1, 1, "2026-11-07T19:00:00-03:00")))
                .andExpect(status().isOk());
    }

    @Test
    void soloSeReprogramaUnPartidoProgramado() throws Exception {
        int id = programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));
        cambiarEstadoEnBase(id, "EN_JUEGO");

        mockMvc.perform(comoAdmin(put("/api/partidos/" + id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, norte, sur, 1, 1, SABADO_18)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Solo se puede modificar un partido PROGRAMADO; este está EN_JUEGO"));
    }

    @Test
    void reprogramarUnIdInexistenteOInvalido() throws Exception {
        mockMvc.perform(comoAdmin(put("/api/partidos/9999"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, norte, sur, 1, 1, SABADO_18)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el partido con id 9999"));

        mockMvc.perform(comoAdmin(put("/api/partidos/0"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, norte, sur, 1, 1, SABADO_18)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El identificador de partido debe ser un entero positivo"));
    }

    // --- Baja (DELETE) ---

    @Test
    void eliminarUnPartidoProgramadoDevuelve204YLoBorra() throws Exception {
        int id = programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));

        mockMvc.perform(comoAdmin(delete("/api/partidos/" + id)))
                .andExpect(status().isNoContent());

        assertThat(partidoRepository.existsById(id)).isFalse();
    }

    @Test
    void soloSeEliminaUnPartidoProgramado() throws Exception {
        int id = programarOk(cuerpo(1, norte, sur, 1, 1, SABADO_18));
        cambiarEstadoEnBase(id, "EN_JUEGO");

        mockMvc.perform(comoAdmin(delete("/api/partidos/" + id)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Solo se puede eliminar un partido PROGRAMADO; este está EN_JUEGO"));
    }

    @Test
    void eliminarUnPartidoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(comoAdmin(delete("/api/partidos/9999")))
                .andExpect(status().isNotFound());
    }

    // --- Permisos (matriz de docs/API.md §2) ---

    @Test
    void sinTokenDevuelve401YComoDelegadoDevuelve403() throws Exception {
        mockMvc.perform(post("/api/partidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, norte, sur, 1, 1, SABADO_18)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/partidos")
                        .with(user("delegado").roles("DELEGADO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, norte, sur, 1, 1, SABADO_18)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/partidos/1").with(user("delegado").roles("DELEGADO")))
                .andExpect(status().isForbidden());
    }

    // --- Utilidades ---

    private int crearEquipo(String nombre) {
        return equipoRepository.save(new Equipo(1, nombre, null)).getId();
    }

    private ResultActions programar(String cuerpo) throws Exception {
        return mockMvc.perform(comoAdmin(post("/api/partidos"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
    }

    private int programarOk(String cuerpo) throws Exception {
        String respuesta = programar(cuerpo)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(respuesta).get("id").asInt();
    }

    /** Simula el cambio de estado de RF-07, que todavía no tiene endpoint. */
    private void cambiarEstadoEnBase(int partidoId, String estado) {
        entityManager.flush();
        jdbcTemplate.update("UPDATE partido SET estado = ? WHERE id = ?", estado, partidoId);
        entityManager.clear();
    }

    private MockHttpServletRequestBuilder comoAdmin(MockHttpServletRequestBuilder builder) {
        return builder.with(user("admin").roles("ADMIN"));
    }

    private static String cuerpo(int numeroFecha, int local, int visitante, int cancha, int arbitro, String inicio) {
        return cuerpoCon(1, numeroFecha, local, visitante, cancha, arbitro, inicio);
    }

    private static String cuerpoCon(int torneo, int numeroFecha, int local, int visitante, int cancha, int arbitro,
                                    String inicio) {
        return """
                {"torneoId":%d,"numeroFecha":%d,"equipoLocalId":%d,"equipoVisitanteId":%d,\
                "canchaId":%d,"arbitroId":%d,"fechaHoraInicio":"%s"}"""
                .formatted(torneo, numeroFecha, local, visitante, cancha, arbitro, inicio);
    }
}