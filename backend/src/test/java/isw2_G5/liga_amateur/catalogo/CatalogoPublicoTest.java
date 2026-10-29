package isw2_G5.liga_amateur.catalogo;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba los endpoints públicos del catálogo (RF-05) contra una base H2 en memoria.
 *
 * Usa los datos precargados de {@code data.sql} (un torneo, tres canchas y cuatro
 * árbitros) y agrega equipos y jugadores de prueba dentro de una transacción que
 * se revierte al finalizar cada caso.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:catalogo-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always"
})
@AutoConfigureMockMvc
@Transactional
class CatalogoPublicoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void crearDatosDePrueba() {
        jdbcTemplate.update("INSERT INTO equipo (id, torneo_id, nombre, barrio, activo) VALUES (900, 1, 'Barrio Norte', NULL, TRUE)");
        jdbcTemplate.update("INSERT INTO equipo (id, torneo_id, nombre, barrio, activo) VALUES (901, 1, 'Atlético Sur', 'Sur', TRUE)");
        jdbcTemplate.update("INSERT INTO equipo (id, torneo_id, nombre, barrio, activo) VALUES (902, 1, 'Club Antiguo', NULL, FALSE)");

        jdbcTemplate.update("INSERT INTO jugador (id, equipo_id, nombre, apellido, dni, numero_camiseta, activo) "
                + "VALUES (910, 900, 'Juan', 'Pérez', '30111222', 10, TRUE)");
        jdbcTemplate.update("INSERT INTO jugador (id, equipo_id, nombre, apellido, dni, numero_camiseta, activo) "
                + "VALUES (911, 900, 'Ana', 'Gómez', '30111223', NULL, TRUE)");
        jdbcTemplate.update("INSERT INTO jugador (id, equipo_id, nombre, apellido, dni, numero_camiseta, activo) "
                + "VALUES (912, 900, 'Luis', 'Pérez', '30111224', 7, FALSE)");

        jdbcTemplate.update("INSERT INTO cancha (id, nombre, direccion, activo) VALUES (903, 'Cancha Clausurada', NULL, FALSE)");
    }

    @Test
    void torneosDevuelveSoloActivosConElContratoAcordado() throws Exception {
        mockMvc.perform(get("/api/torneos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Apertura 2026"))
                .andExpect(jsonPath("$[0].anio").value(2026))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[0].equipos").doesNotExist());
    }

    @Test
    void canchasDevuelveSoloActivasOrdenadasPorNombre() throws Exception {
        mockMvc.perform(get("/api/canchas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].nombre", contains(
                        "Cancha Municipal Norte", "Complejo El Alero", "Predio La Costanera")))
                .andExpect(jsonPath("$[0].direccion").value("Av. Blas Parera 7400"));
    }

    @Test
    void arbitrosDevuelveSoloActivosOrdenadosPorApellido() throws Exception {
        mockMvc.perform(get("/api/arbitros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].apellido", contains("Bustos", "Ferreyra", "Godoy", "Quiroga")))
                .andExpect(jsonPath("$[0].dni").value("27333444"));
    }

    @Test
    void equiposDelTorneoDevuelveSoloActivos() throws Exception {
        mockMvc.perform(get("/api/torneos/1/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nombre", contains("Atlético Sur", "Barrio Norte")))
                .andExpect(jsonPath("$[0].torneoId").value(1))
                .andExpect(jsonPath("$[0].barrio").value("Sur"))
                .andExpect(jsonPath("$[1].barrio").doesNotExist());
    }

    @Test
    void jugadoresDelEquipoDevuelveSoloActivos() throws Exception {
        mockMvc.perform(get("/api/equipos/900/jugadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].apellido", contains("Gómez", "Pérez")))
                .andExpect(jsonPath("$[0].numeroCamiseta").doesNotExist())
                .andExpect(jsonPath("$[1].numeroCamiseta").value(10))
                .andExpect(jsonPath("$[1].equipoId").value(900));
    }

    @Test
    void equipoSinJugadoresDevuelveListaVacia() throws Exception {
        mockMvc.perform(get("/api/equipos/901/jugadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void torneoInexistenteDevuelve404ConElCuerpoComun() throws Exception {
        mockMvc.perform(get("/api/torneos/9999/equipos"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No existe el torneo con id 9999"))
                .andExpect(jsonPath("$.path").value("/api/torneos/9999/equipos"))
                .andExpect(jsonPath("$.validationErrors", hasSize(0)));
    }

    @Test
    void equipoInexistenteDevuelve404ConElCuerpoComun() throws Exception {
        mockMvc.perform(get("/api/equipos/9999/jugadores"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No existe el equipo con id 9999"));
    }

    @Test
    void identificadorNoPositivoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/torneos/0/equipos"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El identificador de torneo debe ser un entero positivo"));

        mockMvc.perform(get("/api/equipos/-5/jugadores"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void identificadorNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/torneos/abc/equipos"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Solicitud inválida"));
    }
}
