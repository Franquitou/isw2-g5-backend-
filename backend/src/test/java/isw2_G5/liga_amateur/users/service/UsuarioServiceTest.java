package isw2_G5.liga_amateur.users.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import isw2_G5.liga_amateur.users.dto.PasswordUpdateRequest;
import isw2_G5.liga_amateur.users.dto.UsuarioCreateRequest;
import isw2_G5.liga_amateur.users.dto.UsuarioEstadoRequest;
import isw2_G5.liga_amateur.users.dto.UsuarioResponse;
import isw2_G5.liga_amateur.users.dto.UsuarioUpdateRequest;
import isw2_G5.liga_amateur.users.entity.Rol;
import isw2_G5.liga_amateur.users.entity.Usuario;
import isw2_G5.liga_amateur.users.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

/** Pruebas de negocio RF-02/RN-08/RN-09 sin Spring ni H2 en archivo. */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository users;

    @Mock
    private EntityManager entityManager;

    private PasswordEncoder passwords;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        passwords = new BCryptPasswordEncoder();
        service = new UsuarioService(users, passwords);
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
    }

    @Test
    void listFiltersActivityAndMapsDtoWithoutHash() {
        Usuario active = user(2, "delegado", Rol.DELEGADO);
        when(users.findByActivo(true)).thenReturn(List.of(active));
        when(users.findByActivo(false)).thenReturn(List.of());

        assertThat(service.list(true)).singleElement().satisfies(dto -> {
            assertThat(dto.username()).isEqualTo("delegado");
            assertThat(dto.rol()).isEqualTo("DELEGADO");
            assertThat(dto.fechaAlta().getOffset().getTotalSeconds()).isEqualTo(-3 * 3600);
            assertThat(dto.toString()).doesNotContain(active.getPasswordHash());
        });
        assertThat(service.list(false)).isEmpty();
    }

    @Test
    void getReturns404ForMissingAnd400ForNonPositiveId() {
        when(users.findById(99)).thenReturn(Optional.empty());
        assertStatus(HttpStatus.NOT_FOUND, () -> service.get(99));
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.get(0));
    }

    @Test
    void createEncodesPasswordAndRefreshesDatabaseGeneratedDate() {
        when(users.saveAndFlush(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 3);
            return user;
        });
        org.mockito.Mockito.doAnswer(invocation -> {
            Usuario refreshed = invocation.getArgument(0, Usuario.class);
            ReflectionTestUtils.setField(refreshed, "fechaAlta",
                    LocalDateTime.of(2026, 10, 29, 14, 30));
            return null;
        }).when(entityManager).refresh(any(Usuario.class));

        UsuarioResponse result = service.create(new UsuarioCreateRequest(
                "nuevo", "contraseña-segura", "Nuevo Usuario", "DELEGADO"));

        ArgumentCaptor<Usuario> saved = ArgumentCaptor.forClass(Usuario.class);
        verify(users).saveAndFlush(saved.capture());
        verify(entityManager).refresh(saved.getValue());
        assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("contraseña-segura");
        assertThat(passwords.matches("contraseña-segura", saved.getValue().getPasswordHash())).isTrue();
        assertThat(result.id()).isEqualTo(3);
        assertThat(result.fechaAlta()).isEqualTo(LocalDateTime.of(2026, 10, 29, 14, 30)
                .atZone(ZoneId.of("America/Argentina/Buenos_Aires")).toOffsetDateTime());
    }

    @Test
    void duplicateUsernameAndDatabaseRaceReturn409() {
        when(users.existsByUsername("usado")).thenReturn(true);
        assertStatus(HttpStatus.CONFLICT, () -> service.create(
                new UsuarioCreateRequest("usado", "12345678", "Usuario", "ADMIN")));
        verify(users, never()).saveAndFlush(any(Usuario.class));

        when(users.saveAndFlush(any(Usuario.class)))
                .thenThrow(new DataIntegrityViolationException("restricción UNIQUE"));
        assertStatus(HttpStatus.CONFLICT, () -> service.create(
                new UsuarioCreateRequest("nuevo", "12345678", "Usuario", "ADMIN")));
    }

    @Test
    void putUpdatesAllEditableFieldsWithoutChangingUsername() {
        Usuario user = user(2, "delegado", Rol.DELEGADO);
        when(users.findById(2)).thenReturn(Optional.of(user));

        UsuarioResponse result = service.update(2, new UsuarioUpdateRequest("Nombre Nuevo", "ADMIN"), "admin");

        assertThat(result.username()).isEqualTo("delegado");
        assertThat(result.nombreCompleto()).isEqualTo("Nombre Nuevo");
        assertThat(result.rol()).isEqualTo("ADMIN");
        assertThat(user.getUsername()).isEqualTo("delegado");
    }

    @Test
    void adminCannotRemoveOwnRole() {
        when(users.findById(1)).thenReturn(Optional.of(user(1, "admin", Rol.ADMIN)));
        assertStatus(HttpStatus.CONFLICT, () -> service.update(
                1, new UsuarioUpdateRequest("Admin", "DELEGADO"), "admin"));
    }

    @Test
    void passwordChangeUsesBcryptAndNeverReturnsHash() {
        Usuario user = user(2, "delegado", Rol.DELEGADO);
        when(users.findById(2)).thenReturn(Optional.of(user));

        UsuarioResponse response = service.updatePassword(2, new PasswordUpdateRequest("clave-nueva"));

        assertThat(passwords.matches("clave-nueva", user.getPasswordHash())).isTrue();
        assertThat(response.toString()).doesNotContain("clave-nueva", user.getPasswordHash());
    }

    @Test
    void adminCannotDeactivateSelfButCanDeactivateAndReactivateOthers() {
        Usuario admin = user(1, "admin", Rol.ADMIN);
        Usuario delegate = user(2, "delegado", Rol.DELEGADO);
        when(users.findById(1)).thenReturn(Optional.of(admin));
        when(users.findById(2)).thenReturn(Optional.of(delegate));

        assertStatus(HttpStatus.CONFLICT,
                () -> service.updateStatus(1, new UsuarioEstadoRequest(false), "admin"));
        assertThat(admin.isActivo()).isTrue();
        assertThat(service.updateStatus(2, new UsuarioEstadoRequest(false), "admin").activo()).isFalse();
        assertThat(service.updateStatus(2, new UsuarioEstadoRequest(true), "admin").activo()).isTrue();
        verify(users, never()).delete(any(Usuario.class));
    }

    private Usuario user(int id, String username, Rol rol) {
        Usuario user = new Usuario(username, passwords.encode("clave-anterior"), "Nombre Original", rol);
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "fechaAlta", LocalDateTime.of(2026, 10, 29, 14, 30));
        return user;
    }

    private void assertStatus(HttpStatus expected, org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action).isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(expected));
    }
}
