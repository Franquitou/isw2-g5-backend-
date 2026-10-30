package isw2_G5.liga_amateur.users.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import isw2_G5.liga_amateur.security.JwtService;
import isw2_G5.liga_amateur.security.JwtService.JwtIdentity;
import isw2_G5.liga_amateur.security.SecurityConfiguration;
import isw2_G5.liga_amateur.security.SecurityErrorResponseWriter;
import isw2_G5.liga_amateur.users.dto.PasswordUpdateRequest;
import isw2_G5.liga_amateur.users.dto.UsuarioCreateRequest;
import isw2_G5.liga_amateur.users.dto.UsuarioEstadoRequest;
import isw2_G5.liga_amateur.users.dto.UsuarioResponse;
import isw2_G5.liga_amateur.users.dto.UsuarioUpdateRequest;
import isw2_G5.liga_amateur.users.entity.Rol;
import isw2_G5.liga_amateur.users.entity.Usuario;
import isw2_G5.liga_amateur.users.repository.UsuarioRepository;
import isw2_G5.liga_amateur.users.service.UsuarioService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

/** Prueba rutas RF-02, roles y errores en MVC aislado sin usar H2 en archivo. */
@WebMvcTest(controllers = UsuarioController.class)
@Import({SecurityConfiguration.class, SecurityErrorResponseWriter.class})
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.datasource.url=jdbc:h2:mem:usuarios_controller;DB_CLOSE_DELAY=-1"
})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private UsuarioRepository repository;

    @MockitoBean
    private JwtService jwt;

    @MockitoBean
    private UsuarioService service;

    @Test
    void mvcSliceDoesNotInitializePersistentDatabase() {
        assertThat(context.getBeanNamesForType(DataSource.class)).isEmpty();
    }

    @Test
    void missingTokenReturns401AndDelegateReturns403() throws Exception {
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        tokenFor("delegado", Rol.DELEGADO);
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer token-delegado"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void adminListsActiveByDefaultAndFiltersInactive() throws Exception {
        tokenFor("admin", Rol.ADMIN);
        when(service.list(true)).thenReturn(List.of(response()));
        when(service.list(false)).thenReturn(List.of());

        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("delegado"))
                .andExpect(jsonPath("$[0].fechaAlta").value("2026-10-29T14:30:00-03:00"))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
        mvc.perform(get("/api/usuarios?activo=false").header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        verify(service).list(true);
        verify(service).list(false);
    }

    @Test
    void invalidActivityFilterReturns400() throws Exception {
        tokenFor("admin", Rol.ADMIN);
        mvc.perform(get("/api/usuarios?activo=otro").header("Authorization", "Bearer token-admin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void adminCanGetAndCreateWithDtoWithoutHash() throws Exception {
        tokenFor("admin", Rol.ADMIN);
        when(service.get(2)).thenReturn(response());
        when(service.create(any(UsuarioCreateRequest.class))).thenReturn(response());

        mvc.perform(get("/api/usuarios/2").header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
        mvc.perform(post("/api/usuarios").header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"delegado\",\"password\":\"clave-segura\",\"nombreCompleto\":\"Nombre Original\",\"rol\":\"DELEGADO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("DELEGADO"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void adminCanEditPasswordAndEstado() throws Exception {
        tokenFor("admin", Rol.ADMIN);
        when(service.update(eq(2), any(UsuarioUpdateRequest.class), eq("admin"))).thenReturn(response());
        when(service.updatePassword(eq(2), any(PasswordUpdateRequest.class))).thenReturn(response());
        when(service.updateStatus(eq(2), any(UsuarioEstadoRequest.class), eq("admin"))).thenReturn(response());

        mvc.perform(put("/api/usuarios/2").header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCompleto\":\"Nombre Original\",\"rol\":\"DELEGADO\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/usuarios/2/password").header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"clave-nueva\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/usuarios/2/estado").header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":false}"))
                .andExpect(status().isOk());
        verify(service).update(2, new UsuarioUpdateRequest("Nombre Original", "DELEGADO"), "admin");
        verify(service).updatePassword(2, new PasswordUpdateRequest("clave-nueva"));
        verify(service).updateStatus(2, new UsuarioEstadoRequest(false), "admin");
    }

    @Test
    void invalidBodyReturns400AndMissingUserReturns404() throws Exception {
        tokenFor("admin", Rol.ADMIN);
        mvc.perform(post("/api/usuarios").header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"corta\",\"nombreCompleto\":\"\",\"rol\":\"OTRO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
        when(service.get(99)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mvc.perform(get("/api/usuarios/99").header("Authorization", "Bearer token-admin"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void selfProtectionConflictReturnsCommon409() throws Exception {
        tokenFor("admin", Rol.ADMIN);
        when(service.updateStatus(eq(1), any(UsuarioEstadoRequest.class), eq("admin")))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));
        mvc.perform(patch("/api/usuarios/1/estado").header("Authorization", "Bearer token-admin")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Conflicto con los datos o el estado del recurso"));
    }

    private void tokenFor(String username, Rol rol) {
        when(jwt.verify("token-" + username)).thenReturn(new JwtIdentity(username, rol));
        when(repository.findByUsername(username))
                .thenReturn(Optional.of(new Usuario(username, "hash-no-expuesto", "Nombre Original", rol)));
    }

    private UsuarioResponse response() {
        return new UsuarioResponse(2, "delegado", "Nombre Original", "DELEGADO", true,
                OffsetDateTime.parse("2026-10-29T14:30:00-03:00"));
    }
}
