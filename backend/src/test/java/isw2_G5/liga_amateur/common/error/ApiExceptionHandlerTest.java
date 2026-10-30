package isw2_G5.liga_amateur.common.error;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Prueba el contrato JSON de errores sin iniciar Spring Boot ni la base H2. */
class ApiExceptionHandlerTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void validationReturnsFieldAndCommonEnvelope() throws Exception {
        mvc.perform(post("/validacion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La solicitud contiene datos inválidos"))
                .andExpect(jsonPath("$.timestamp", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.path").value("/validacion"))
                .andExpect(jsonPath("$.validationErrors", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].field").value("name"))
                .andExpect(jsonPath("$.validationErrors[0].message", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.validationErrors[0].rejectedValue").doesNotExist());
    }

    @Test
    void malformedBodyReturns400WithoutInternalDetails() throws Exception {
        mvc.perform(post("/validacion").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Solicitud inválida"))
                .andExpect(jsonPath("$.path").value("/validacion"))
                .andExpect(jsonPath("$.validationErrors", hasSize(0)));
    }

    @Test
    void unsupportedMethodKeepsHttpStatus() throws Exception {
        mvc.perform(post("/saludo"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value("Método HTTP no permitido"))
                .andExpect(jsonPath("$.path").value("/saludo"));
    }

    @Test
    void missingResourceKeeps404WithoutExposingReason() throws Exception {
        mvc.perform(get("/no-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.path").value("/no-encontrado"));
    }

    @Test
    void unexpectedErrorHidesSensitiveMessage() throws Exception {
        mvc.perform(get("/fallo"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error interno del servidor"))
                .andExpect(jsonPath("$.timestamp", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.path").value("/fallo"))
                .andExpect(jsonPath("$.validationErrors", hasSize(0)))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void conflictingBusinessOperationUsesCommon409() throws Exception {
        mvc.perform(get("/conflicto"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Conflicto con los datos o el estado del recurso"))
                .andExpect(jsonPath("$.path").value("/conflicto"));
    }

    @RestController
    static class TestController {
        @PostMapping("/validacion")
        void validate(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/saludo")
        String greeting() {
            return "ok";
        }

        @GetMapping("/no-encontrado")
        void missing() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "detalle privado");
        }

        @GetMapping("/fallo")
        void fail() {
            throw new IllegalStateException("detalle interno sensible");
        }

        @GetMapping("/conflicto")
        void conflict() {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }

    record TestRequest(@NotBlank String name) {
    }
}
