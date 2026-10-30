package isw2_G5.liga_amateur.users.service;

import isw2_G5.liga_amateur.users.dto.PasswordUpdateRequest;
import isw2_G5.liga_amateur.users.dto.UsuarioCreateRequest;
import isw2_G5.liga_amateur.users.dto.UsuarioEstadoRequest;
import isw2_G5.liga_amateur.users.dto.UsuarioResponse;
import isw2_G5.liga_amateur.users.dto.UsuarioUpdateRequest;
import isw2_G5.liga_amateur.users.entity.Rol;
import isw2_G5.liga_amateur.users.entity.Usuario;
import isw2_G5.liga_amateur.users.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.ZoneId;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Gestiona las cuentas de la liga conforme a RF-02: contraseña cifrada, username
 * inmutable, baja lógica y protección de la propia cuenta administrativa (RN-08/09).
 */
@Service
@Transactional
public class UsuarioService {

    private static final ZoneId ZONA_LIGA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final UsuarioRepository users;
    private final PasswordEncoder passwords;

    @PersistenceContext
    private EntityManager entityManager;

    public UsuarioService(UsuarioRepository users, PasswordEncoder passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    /** Consulta cuentas por actividad; por defecto el controlador solicita las activas. */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> list(boolean activo) {
        return users.findByActivo(activo).stream().map(this::toResponse).toList();
    }

    /** @throws ResponseStatusException 400 para ID inválido o 404 si no existe. */
    @Transactional(readOnly = true)
    public UsuarioResponse get(Integer id) {
        return toResponse(findUser(id));
    }

    /**
     * Da de alta una cuenta con BCrypt; la restricción UNIQUE de SQL cubre también
     * altas simultáneas que superen la comprobación previa de username.
     */
    public UsuarioResponse create(UsuarioCreateRequest request) {
        if (users.existsByUsername(request.username())) {
            throw conflict();
        }
        Usuario user = new Usuario(request.username(), passwords.encode(request.password()),
                request.nombreCompleto(), Rol.valueOf(request.rol()));
        try {
            users.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw conflict();
        }
        // fecha_alta tiene DEFAULT en SQL y no se inserta desde JPA.
        entityManager.refresh(user);
        return toResponse(user);
    }

    /** Modifica todos los campos editables de PUT, sin alterar el username. */
    public UsuarioResponse update(Integer id, UsuarioUpdateRequest request, String actorUsername) {
        Usuario user = findUser(id);
        Rol newRole = Rol.valueOf(request.rol());
        if (user.getUsername().equals(actorUsername) && user.getRol() == Rol.ADMIN && newRole != Rol.ADMIN) {
            throw conflict();
        }
        user.actualizar(request.nombreCompleto(), newRole);
        return toResponse(user);
    }

    /** Reemplaza el hash BCrypt; la contraseña nunca viaja en la respuesta. */
    public UsuarioResponse updatePassword(Integer id, PasswordUpdateRequest request) {
        Usuario user = findUser(id);
        user.cambiarPasswordHash(passwords.encode(request.password()));
        return toResponse(user);
    }

    /** Desactiva o reactiva sin DELETE físico; RN-08 impide desactivarse a sí mismo. */
    public UsuarioResponse updateStatus(Integer id, UsuarioEstadoRequest request, String actorUsername) {
        Usuario user = findUser(id);
        if (user.getUsername().equals(actorUsername) && user.getRol() == Rol.ADMIN && !request.activo()) {
            throw conflict();
        }
        user.cambiarActivo(request.activo());
        return toResponse(user);
    }

    private Usuario findUser(Integer id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private UsuarioResponse toResponse(Usuario user) {
        return new UsuarioResponse(user.getId(), user.getUsername(), user.getNombreCompleto(),
                user.getRol().name(), user.isActivo(),
                user.getFechaAlta().atZone(ZONA_LIGA).toOffsetDateTime());
    }

    private ResponseStatusException conflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT);
    }
}
