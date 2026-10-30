package isw2_G5.liga_amateur.security.auth;

import isw2_G5.liga_amateur.security.JwtService;
import isw2_G5.liga_amateur.users.entity.Usuario;
import isw2_G5.liga_amateur.users.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Valida credenciales con BCrypt y rechaza usuarios inactivos antes de emitir un JWT. */
@Service
public class AuthService {

    private final UsuarioRepository users;
    private final PasswordEncoder passwords;
    private final JwtService jwt;

    public AuthService(UsuarioRepository users, PasswordEncoder passwords, JwtService jwt) {
        this.users = users;
        this.passwords = passwords;
        this.jwt = jwt;
    }

    /**
     * Autentica sin distinguir ante el cliente si falló username, contraseña o actividad.
     *
     * @param request credenciales solicitadas
     * @return token, nombre y rol del usuario activo
     * @throws ResponseStatusException con HTTP 401 cuando las credenciales no permiten ingresar
     */
    public LoginResponse login(LoginRequest request) {
        Usuario user = users.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!user.isActivo() || !passwords.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return new LoginResponse(jwt.generate(user.getUsername(), user.getRol()),
                user.getNombreCompleto(), user.getRol());
    }
}
