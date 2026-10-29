package isw2_G5.liga_amateur.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import isw2_G5.liga_amateur.catalogo.repository.EquipoRepository;
import isw2_G5.liga_amateur.catalogo.repository.JugadorRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba los endpoints públicos y de administración de equipos y jugadores
 * (RF-03, RF-04 y RF-05) contra una base H2 en memoria con los datos de
 * {@code schema.sql} y {@code data.sql}.
 *
 * <p>La seguridad real todavía no incluye JWT, así que los permisos se
 * simulan con usuarios de Spring Security Test.</p>
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:equipos-jugadores-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always"
})
@AutoConfigureMockMvc
@Transactional
class EquiposJugadoresApiTest {

    private static final String ADMIN = "admin";
    private static final String DELEGADO = "delegado";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    // --- Consultas públicas (RF-05) ---

    @Test
    void losTorneosSonPublicosYSoloDevuelvenActivos() throws Exception {
        mockMvc.perform(get("/api/torneos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Apertura 2026"))
                .andExpect(jsonPath("$[0].anio").value(2026));
    }

    @Test
    void losEquiposDeUnTorneoInexistenteDevuelven404() throws Exception {
        mockMvc.perform(get("/api/torneos/9999/equipos"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No existe el torneo con id 9999"));
    }

    @Test
    void losEquiposDeUnEquipoInexistenteDevuelven404() throws Exception {
        mockMvc.perform(get("/api/equipos/9999/jugadores"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el equipo con id 9999"));
    }

    @Test
    void unIdentificadorNoPositivoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/torneos/0/equipos"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El identificador de torneo debe ser un entero positivo"));

        mockMvc.perform(get("/api/equipos/-3/jugadores"))
                .andExpect(status().isBadRequest());
    }

    // --- RF-03: equipos ---

    @Test
    void crearEquipoDevuelve201YApareceEnElListado() throws Exception {
        mockMvc.perform(comoAdmin(post("/api/torneos/1/equipos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Barrio Norte\",\"barrio\":\"Norte\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.torneoId").value(1))
                .andExpect(jsonPath("$.nombre").value("Barrio Norte"))
                .andExpect(jsonPath("$.barrio").value("Norte"))
                .andExpect(jsonPath("$.activo").value(true));

        mockMvc.perform(get("/api/torneos/1/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].nombre", contains("Barrio Norte")));
    }

    @Test
    void unBarrioVacioSeGuardaComoNulo() throws Exception {
        mockMvc.perform(comoAdmin(post("/api/torneos/1/equipos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"  Barrio Sur  \",\"barrio\":\"   \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Barrio Sur"))
                .andExpect(jsonPath("$.barrio").doesNotExist());
    }

    @Test
    void unNombreRepetidoEnElTorneoDevuelve409() throws Exception {
        crearEquipo("Barrio Norte");

        mockMvc.perform(comoAdmin(post("/api/torneos/1/equipos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Barrio Norte\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Ya existe un equipo con ese nombre en el torneo"));
    }

    @Test
    void unNombreVacioDevuelve400ConElCampoAfectado() throws Exception {
        mockMvc.perform(comoAdmin(post("/api/torneos/1/equipos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors[0].field").value("nombre"));
    }

    @Test
    void crearEquipoEnUnTorneoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(comoAdmin(post("/api/torneos/9999/equipos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Barrio Norte\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el torneo con id 9999"));
    }

    @Test
    void actualizarEquipoConservaElTorneo() throws Exception {
        int id = crearEquipo("Barrio Norte");

        mockMvc.perform(comoAdmin(put("/api/equipos/" + id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Barrio Norte Unido\",\"barrio\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.torneoId").value(1))
                .andExpect(jsonPath("$.nombre").value("Barrio Norte Unido"))
                .andExpect(jsonPath("$.barrio").doesNotExist());
    }

    @Test
    void actualizarConElNombreDeOtroEquipoDevuelve409() throws Exception {
        crearEquipo("Barrio Norte");
        int otro = crearEquipo("Barrio Sur");

        mockMvc.perform(comoAdmin(put("/api/equipos/" + otro))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Barrio Norte\",\"barrio\":null}"))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminarEquipoAplicaBajaLogica() throws Exception {
        int id = crearEquipo("Barrio Norte");

        mockMvc.perform(comoAdmin(delete("/api/equipos/" + id)))
                .andExpect(status().isNoContent());

        assertThat(equipoRepository.findById(id).orElseThrow().isActivo()).isFalse();
        mockMvc.perform(get("/api/torneos/1/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- RF-04: jugadores ---

    @Test
    void crearJugadorDevuelve201YApareceEnElListado() throws Exception {
        int equipoId = crearEquipo("Barrio Norte");

        mockMvc.perform(comoAdmin(post("/api/equipos/" + equipoId + "/jugadores"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Juan\",\"apellido\":\"Pérez\",\"dni\":\"30111222\",\"numeroCamiseta\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipoId").value(equipoId))
                .andExpect(jsonPath("$.dni").value("30111222"))
                .andExpect(jsonPath("$.numeroCamiseta").value(10))
                .andExpect(jsonPath("$.activo").value(true));

        mockMvc.perform(get("/api/equipos/" + equipoId + "/jugadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].apellido", contains("Pérez")));
    }

    @Test
    void unDniRepetidoDevuelve409() throws Exception {
        int equipoId = crearEquipo("Barrio Norte");
        crearJugador(equipoId, "Juan", "Pérez", "30111222", 10);

        mockMvc.perform(comoAdmin(post("/api/equipos/" + equipoId + "/jugadores"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"apellido\":\"Gómez\",\"dni\":\"30111222\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe un jugador con ese DNI"));
    }

    @Test
    void unaCamisetaFueraDeRangoDevuelve400() throws Exception {
        int equipoId = crearEquipo("Barrio Norte");

        mockMvc.perform(comoAdmin(post("/api/equipos/" + equipoId + "/jugadores"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Juan\",\"apellido\":\"Pérez\",\"dni\":\"30111222\",\"numeroCamiseta\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("numeroCamiseta"));
    }

    @Test
    void crearJugadorEnUnEquipoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(comoAdmin(post("/api/equipos/9999/jugadores"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Juan\",\"apellido\":\"Pérez\",\"dni\":\"30111222\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarJugadorMantieneElEquipo() throws Exception {
        int equipoId = crearEquipo("Barrio Norte");
        int jugadorId = crearJugador(equipoId, "Juan", "Pérez", "30111222", 10);

        mockMvc.perform(comoAdmin(put("/api/jugadores/" + jugadorId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Juan Carlos\",\"apellido\":\"Pérez\",\"dni\":\"30111222\",\"numeroCamiseta\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipoId").value(equipoId))
                .andExpect(jsonPath("$.nombre").value("Juan Carlos"))
                .andExpect(jsonPath("$.numeroCamiseta").doesNotExist());
    }

    @Test
    void eliminarJugadorAplicaBajaLogica() throws Exception {
        int equipoId = crearEquipo("Barrio Norte");
        int jugadorId = crearJugador(equipoId, "Juan", "Pérez", "30111222", null);

        mockMvc.perform(comoAdmin(delete("/api/jugadores/" + jugadorId)))
                .andExpect(status().isNoContent());

        assertThat(jugadorRepository.findById(jugadorId).orElseThrow().isActivo()).isFalse();
        mockMvc.perform(get("/api/equipos/" + equipoId + "/jugadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- Seguridad ---

    @Test
    void escribirSinAutenticacionDevuelve401ConElCuerpoComun() throws Exception {
        mockMvc.perform(post("/api/torneos/1/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Barrio Norte\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Autenticación requerida"));
    }

    @Test
    void escribirComoDelegadoDevuelve403() throws Exception {
        mockMvc.perform(post("/api/torneos/1/equipos")
                        .with(user(DELEGADO).roles("DELEGADO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Barrio Norte\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acceso denegado"));
    }

    // --- Utilidades ---

    private MockHttpServletRequestBuilder comoAdmin(MockHttpServletRequestBuilder builder) {
        return builder.with(user(ADMIN).roles("ADMIN"));
    }

    private int crearEquipo(String nombre) throws Exception {
        String respuesta = mockMvc.perform(comoAdmin(post("/api/torneos/1/equipos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(respuesta).get("id").asInt();
    }

    private int crearJugador(int equipoId, String nombre, String apellido, String dni, Integer camiseta)
            throws Exception {
        String cuerpo = objectMapper.writeValueAsString(new JugadorFixture(nombre, apellido, dni, camiseta));
        String respuesta = mockMvc.perform(comoAdmin(post("/api/equipos/" + equipoId + "/jugadores"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(respuesta).get("id").asInt();
    }

    /** Cuerpo de alta de jugador usado por las pruebas. */
    private record JugadorFixture(String nombre, String apellido, String dni, Integer numeroCamiseta) {
    }
}
