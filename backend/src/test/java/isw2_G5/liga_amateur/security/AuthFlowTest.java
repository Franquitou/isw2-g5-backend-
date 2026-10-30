package isw2_G5.liga_amateur.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import isw2_G5.liga_amateur.security.auth.AuthController;
import isw2_G5.liga_amateur.security.auth.AuthService;
import isw2_G5.liga_amateur.users.entity.Rol;
import isw2_G5.liga_amateur.users.entity.Usuario;
import isw2_G5.liga_amateur.users.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Verifica login, BCrypt, JWT y permisos en una misma cadena MVC sin iniciar H2 en archivo.
 * El repositorio se simula, SQL queda desactivado y la clave de prueba es efímera.
 */
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfiguration.class, SecurityErrorResponseWriter.class, PasswordConfiguration.class,
        JwtService.class, AuthService.class, AuthFlowTest.ProtectedRoutes.class})
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.datasource.url=jdbc:h2:mem:auth_flow_test;DB_CLOSE_DELAY=-1"
})
class AuthFlowTest {

    private static final String TEST_SECRET = randomSecret();

    @DynamicPropertySource
    static void jwtSecret(DynamicPropertyRegistry registry) {
        registry.add("LIGA_JWT_SECRET", () -> TEST_SECRET);
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PasswordEncoder passwords;

    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private UsuarioRepository users;

    @Test
    void mvcSliceHasNoDataSource() {
        assertThat(context.getBeanNamesForType(DataSource.class)).isEmpty();
    }

    @Test
    void publicQueryWorksWithoutLogin() throws Exception {
        mvc.perform(get("/api/torneos")).andExpect(status().isOk());
    }

    @Test
    void adminLoginThenBearerAccessSucceedsWithoutExposingHash() throws Exception {
        givenUser("admin", Rol.ADMIN, true);

        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"clave-segura\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCompleto").value("Usuario de prueba"))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(body).get("token").asText();

        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void delegateTokenCannotEnterAdminRoute() throws Exception {
        givenUser("delegado1", Rol.DELEGADO, true);
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"delegado1\",\"password\":\"clave-segura\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(response).get("token").asText();

        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Acceso denegado"));
    }

    @Test
    void inactiveAccountAndWrongPasswordShare401() throws Exception {
        givenUser("admin", Rol.ADMIN, false);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"clave-segura\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticación requerida"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticación requerida"));
    }

    @Test
    void deactivatedAccountCannotReusePreviouslyIssuedToken() throws Exception {
        Usuario user = givenUser("admin", Rol.ADMIN, true);
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"clave-segura\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        user.cambiarActivo(false);

        mvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + mapper.readTree(body).get("token").asText()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void expiredTokenCannotAccessProtectedRoute() throws Exception {
        String expired = Jwts.builder().subject("admin").claim("rol", "ADMIN")
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/usuarios"));
    }

    private Usuario givenUser(String username, Rol rol, boolean activo) {
        Usuario user = new Usuario(username, passwords.encode("clave-segura"), "Usuario de prueba", rol);
        user.cambiarActivo(activo);
        when(users.findByUsername(username)).thenReturn(Optional.of(user));
        return user;
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @RestController
    public static class ProtectedRoutes {

        @GetMapping("/api/torneos")
        public String torneos() {
            return "público";
        }

        @GetMapping("/api/usuarios")
        public String usuarios() {
            return "admin";
        }
    }
}
