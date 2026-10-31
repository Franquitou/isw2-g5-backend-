package isw2_G5.liga_amateur.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifica que la app web de Expo pueda llamar a la API desde otro origen (CORS).
 * Usa una H2 en memoria y no toca {@code ./data/liga_amateur}.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cors-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always"
})
@AutoConfigureMockMvc
class CorsConfigTest {

    private static final String ORIGEN_WEB = "http://localhost:8081";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void elPreflightDelWebDevSePermite() throws Exception {
        mockMvc.perform(options("/api/torneos")
                        .header(HttpHeaders.ORIGIN, ORIGEN_WEB)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEN_WEB));
    }

    @Test
    void unaLecturaPublicaDevuelveLaCabeceraCors() throws Exception {
        mockMvc.perform(get("/api/torneos").header(HttpHeaders.ORIGIN, ORIGEN_WEB))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEN_WEB));
    }

    @Test
    void unOrigenAjenoSeRechaza() throws Exception {
        mockMvc.perform(options("/api/torneos")
                        .header(HttpHeaders.ORIGIN, "https://ejemplo-ajeno.test")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}
