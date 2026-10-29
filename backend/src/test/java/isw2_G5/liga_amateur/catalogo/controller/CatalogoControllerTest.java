package isw2_G5.liga_amateur.catalogo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import isw2_G5.liga_amateur.catalogo.dto.ArbitroResponse;
import isw2_G5.liga_amateur.catalogo.dto.CanchaResponse;
import isw2_G5.liga_amateur.catalogo.service.CatalogoService;
import isw2_G5.liga_amateur.common.error.ApiExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Prueba las rutas y el JSON de {@code docs/API.md} sin iniciar Spring Boot ni la base H2. */
class CatalogoControllerTest {

    private CatalogoService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(CatalogoService.class);
        mvc = MockMvcBuilders.standaloneSetup(new CatalogoController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void listaCanchasConLosCamposDelContrato() throws Exception {
        when(service.listarCanchasActivas()).thenReturn(List.of(
                new CanchaResponse(1, "Cancha Municipal Norte", "Av. Blas Parera 7400", true),
                new CanchaResponse(4, "Cancha Sin Dirección", null, true)));

        mvc.perform(get("/api/canchas"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Cancha Municipal Norte"))
                .andExpect(jsonPath("$[0].direccion").value("Av. Blas Parera 7400"))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[1].direccion", nullValue()));
    }

    @Test
    void listaArbitrosConLosCamposDelContrato() throws Exception {
        when(service.listarArbitrosActivos()).thenReturn(List.of(
                new ArbitroResponse(3, "Hernán", "Bustos", "27333444", true)));

        mvc.perform(get("/api/arbitros"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].nombre").value("Hernán"))
                .andExpect(jsonPath("$[0].apellido").value("Bustos"))
                .andExpect(jsonPath("$[0].dni").value("27333444"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void devuelveArregloVacioSinRegistros() throws Exception {
        when(service.listarCanchasActivas()).thenReturn(List.of());

        mvc.perform(get("/api/canchas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void noAdmiteEscrituraDeCanchasNiArbitros() throws Exception {
        mvc.perform(post("/api/canchas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.path").value("/api/canchas"));

        mvc.perform(post("/api/arbitros").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }
}