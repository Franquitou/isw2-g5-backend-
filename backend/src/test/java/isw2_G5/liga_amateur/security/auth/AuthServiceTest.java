package isw2_G5.liga_amateur.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import isw2_G5.liga_amateur.security.JwtService;
import isw2_G5.liga_amateur.users.entity.Rol;
import isw2_G5.liga_amateur.users.entity.Usuario;
import isw2_G5.liga_amateur.users.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

/** Comprueba RF-01 con un repositorio simulado, sin iniciar Spring ni H2. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository users;

    @Mock
    private JwtService jwt;

    private AuthService auth;
    private PasswordEncoder passwords;

    @BeforeEach
    void setUp() {
        passwords = new BCryptPasswordEncoder();
        auth = new AuthService(users, passwords, jwt);
    }

    @Test
    void correctCredentialsReturnTokenNameAndRoleWithoutHash() {
        Usuario user = new Usuario("delegado1", passwords.encode("contraseña-segura"),
                "Delegado Uno", Rol.DELEGADO);
        when(users.findByUsername("delegado1")).thenReturn(Optional.of(user));
        when(jwt.generate("delegado1", Rol.DELEGADO)).thenReturn("jwt-de-prueba");

        LoginResponse result = auth.login(new LoginRequest("delegado1", "contraseña-segura"));

        assertThat(result).isEqualTo(new LoginResponse("jwt-de-prueba", "Delegado Uno", Rol.DELEGADO));
        assertThat(result.toString()).doesNotContain("contraseña-segura", user.getPasswordHash());
    }

    @Test
    void unknownUsernameUsesSame401AsWrongPassword() {
        when(users.findByUsername("desconocido")).thenReturn(Optional.empty());
        Usuario user = new Usuario("admin", passwords.encode("contraseña-segura"), "Admin", Rol.ADMIN);
        when(users.findByUsername("admin")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> auth.login(new LoginRequest("desconocido", "otra")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        assertThatThrownBy(() -> auth.login(new LoginRequest("admin", "incorrecta")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        verify(jwt, never()).generate(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void inactiveUserIsRejectedDespiteCorrectPassword() {
        Usuario user = new Usuario("admin", passwords.encode("contraseña-segura"), "Admin", Rol.ADMIN);
        user.cambiarActivo(false);
        when(users.findByUsername("admin")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> auth.login(new LoginRequest("admin", "contraseña-segura")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        verify(jwt, never()).generate(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }
}
