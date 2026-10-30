package isw2_G5.liga_amateur.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.JwtException;
import isw2_G5.liga_amateur.security.JwtService.JwtIdentity;
import isw2_G5.liga_amateur.users.entity.Rol;
import isw2_G5.liga_amateur.users.entity.Usuario;
import isw2_G5.liga_amateur.users.repository.UsuarioRepository;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

/** Prueba el filtro Bearer directamente, sin iniciar la aplicación ni H2. */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwt;

    @Mock
    private UsuarioRepository users;

    @Mock
    private AuthenticationEntryPoint entryPoint;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwt, users, entryPoint);
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenUsesActiveAccountAndItsRole() throws Exception {
        MockHttpServletRequest request = bearer("firma-valida");
        when(jwt.verify("firma-valida")).thenReturn(new JwtIdentity("admin", Rol.ADMIN));
        when(users.findByUsername("admin"))
                .thenReturn(Optional.of(new Usuario("admin", "hash", "Admin", Rol.ADMIN)));
        AtomicReference<Authentication> insideChain = new AtomicReference<>();

        filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> insideChain.set(SecurityContextHolder.getContext().getAuthentication()));

        assertThat(insideChain.get().getName()).isEqualTo("admin");
        assertThat(insideChain.get().getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(entryPoint, never()).commence(any(), any(), any());
    }

    @Test
    void invalidSignatureInvokesAuthenticationEntryPoint() throws Exception {
        when(jwt.verify("alterado")).thenThrow(new JwtException("Firma inválida"));
        AtomicReference<Boolean> invoked = new AtomicReference<>(false);

        filter.doFilter(bearer("alterado"), new MockHttpServletResponse(),
                (req, res) -> invoked.set(true));

        assertThat(invoked.get()).isFalse();
        verify(entryPoint).commence(any(), any(), any());
    }

    @Test
    void inactiveUserCannotUsePreviouslyIssuedToken() throws Exception {
        when(jwt.verify("vigente")).thenReturn(new JwtIdentity("admin", Rol.ADMIN));
        Usuario user = new Usuario("admin", "hash", "Admin", Rol.ADMIN);
        user.cambiarActivo(false);
        when(users.findByUsername("admin")).thenReturn(Optional.of(user));

        filter.doFilter(bearer("vigente"), new MockHttpServletResponse(),
                (req, res) -> { throw new AssertionError("No debe avanzar al controlador"); });

        verify(entryPoint).commence(any(), any(), any());
    }

    @Test
    void outdatedRoleCannotKeepOldPrivileges() throws Exception {
        when(jwt.verify("vigente")).thenReturn(new JwtIdentity("admin", Rol.ADMIN));
        when(users.findByUsername("admin"))
                .thenReturn(Optional.of(new Usuario("admin", "hash", "Admin", Rol.DELEGADO)));

        filter.doFilter(bearer("vigente"), new MockHttpServletResponse(),
                (req, res) -> { throw new AssertionError("No debe avanzar al controlador"); });

        verify(entryPoint).commence(any(), any(), any());
    }

    @Test
    void absentBearerLetsPublicRoutesPassWithoutAuthentication() throws Exception {
        AtomicReference<Boolean> invoked = new AtomicReference<>(false);
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
                (req, res) -> invoked.set(true));

        assertThat(invoked.get()).isTrue();
        verify(jwt, never()).verify(any());
    }

    private MockHttpServletRequest bearer(String value) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/usuarios");
        request.addHeader("Authorization", "Bearer " + value);
        return request;
    }
}
