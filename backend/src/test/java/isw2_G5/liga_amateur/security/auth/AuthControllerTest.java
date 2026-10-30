package isw2_G5.liga_amateur.security.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import isw2_G5.liga_amateur.common.error.ApiExceptionHandler;
import isw2_G5.liga_amateur.users.entity.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

/** Verifica el contrato de login con MockMvc aislado, sin iniciar la aplicación ni H2. */
class AuthControllerTest {

    private AuthService auth;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        auth = Mockito.mock(AuthService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(auth))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void successfulLoginReturnsOnlyPublicFields() throws Exception {
        when(auth.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("jwt-de-prueba", "Delegado Uno", Rol.DELEGADO));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"delegado1\",\"password\":\"contraseña-segura\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-de-prueba"))
                .andExpect(jsonPath("$.nombreCompleto").value("Delegado Uno"))
                .andExpect(jsonPath("$.rol").value("DELEGADO"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void invalidCredentialsUseCommon401WithoutHint() throws Exception {
        when(auth.login(any(LoginRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"desconocido\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/auth/login"))
                .andExpect(jsonPath("$.message").value("Autenticación requerida"));
    }

    @Test
    void blankUsernameIsRejectedBeforeCallingService() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"contraseña-segura\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("username"));
    }
}
