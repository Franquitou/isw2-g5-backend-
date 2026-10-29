package isw2_G5.liga_amateur.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Comprueba la política HTTP en un contexto MVC limitado a rutas de prueba.
 * No carga JPA ni una base H2 en archivo; las propiedades de respaldo también desactivan SQL.
 */
@WebMvcTest(controllers = SecurityConfigurationTest.SecurityRoutes.class)
@Import({SecurityConfiguration.class, SecurityErrorResponseWriter.class,
        SecurityConfigurationTest.SecurityRoutes.class})
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.datasource.url=jdbc:h2:mem:seguridad_base;DB_CLOSE_DELAY=-1"
})
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApplicationContext context;

    @Test
    void mvcSliceDoesNotConfigurePersistentDatabase() {
        assertThat(context.getBeanNamesForType(DataSource.class)).isEmpty();
    }

    @Test
    void publicCatalogueDoesNotRequireCredentials() throws Exception {
        mvc.perform(get("/api/torneos"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedRouteWithoutTokenUsesCommon401() throws Exception {
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Autenticación requerida"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/usuarios"))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void bearerHeaderAloneDoesNotAuthenticateUntilJwtIsImplemented() throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer no-es-un-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "DELEGADO")
    void delegateCannotAdministerUsers() throws Exception {
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Acceso denegado"))
                .andExpect(jsonPath("$.path").value("/api/usuarios"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessUsersAndScheduleMatches() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().isOk());
        mvc.perform(post("/api/partidos")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DELEGADO")
    void delegateCanChangeMatchStateAndRegisterEventsButCannotSchedule() throws Exception {
        mvc.perform(patch("/api/partidos/1/estado")).andExpect(status().isOk());
        mvc.perform(post("/api/partidos/1/eventos")).andExpect(status().isOk());
        mvc.perform(post("/api/partidos")).andExpect(status().isForbidden());
    }

    @Test
    void unlistedMutationIsNotAccidentallyPublic() throws Exception {
        mvc.perform(post("/api/torneos")).andExpect(status().isUnauthorized());
    }

    @RestController
    public static class SecurityRoutes {
        @GetMapping("/api/torneos")
        public String catalogue() {
            return "ok";
        }

        @PostMapping("/api/torneos")
        public String unexpectedMutation() {
            return "no debe exponerse";
        }

        @GetMapping("/api/usuarios")
        public String users() {
            return "ok";
        }

        @PostMapping("/api/partidos")
        public String schedule() {
            return "ok";
        }

        @PatchMapping("/api/partidos/{id}/estado")
        public String changeState() {
            return "ok";
        }

        @PostMapping("/api/partidos/{id}/eventos")
        public String registerEvent() {
            return "ok";
        }
    }
}
