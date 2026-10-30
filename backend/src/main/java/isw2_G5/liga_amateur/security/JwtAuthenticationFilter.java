package isw2_G5.liga_amateur.security;

import io.jsonwebtoken.JwtException;
import isw2_G5.liga_amateur.security.JwtService.JwtIdentity;
import isw2_G5.liga_amateur.users.entity.Usuario;
import isw2_G5.liga_amateur.users.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Valida el JWT y consulta la cuenta actual antes de conceder un rol.
 * Debe registrarse en la cadena de seguridad al integrar esta etapa con la etapa 1.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";
    private final JwtService jwt;
    private final UsuarioRepository users;
    private final AuthenticationEntryPoint entryPoint;

    public JwtAuthenticationFilter(JwtService jwt, UsuarioRepository users, AuthenticationEntryPoint entryPoint) {
        this.jwt = jwt;
        this.users = users;
        this.entryPoint = entryPoint;
    }

    /** Deja pasar solicitudes sin Bearer; la autorización de rutas decide si requieren sesión. */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            JwtIdentity identity = jwt.verify(authorization.substring(BEARER.length()));
            Usuario user = users.findByUsername(identity.username())
                    .orElseThrow(() -> new BadCredentialsException("Token inválido"));
            if (!user.isActivo() || user.getRol() != identity.rol()) {
                throw new BadCredentialsException("Token inválido");
            }
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    user.getUsername(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol().name()))));
            chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(request, response, new BadCredentialsException("Token inválido", exception));
        } catch (BadCredentialsException exception) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(request, response, exception);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
