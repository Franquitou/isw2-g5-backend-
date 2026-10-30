package isw2_G5.liga_amateur.users.controller;

import isw2_G5.liga_amateur.users.dto.PasswordUpdateRequest;
import isw2_G5.liga_amateur.users.dto.UsuarioCreateRequest;
import isw2_G5.liga_amateur.users.dto.UsuarioEstadoRequest;
import isw2_G5.liga_amateur.users.dto.UsuarioResponse;
import isw2_G5.liga_amateur.users.dto.UsuarioUpdateRequest;
import isw2_G5.liga_amateur.users.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Expone únicamente las operaciones ADMIN de RF-02 con DTOs, nunca entidades JPA. */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService users;

    public UsuarioController(UsuarioService users) {
        this.users = users;
    }

    /** Lista activos por defecto o aplica el filtro administrativo solicitado. */
    @GetMapping
    public List<UsuarioResponse> list(@RequestParam(required = false) Boolean activo) {
        return users.list(activo == null || activo);
    }

    @GetMapping("/{id}")
    public UsuarioResponse get(@PathVariable Integer id) {
        return users.get(id);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> create(@Valid @RequestBody UsuarioCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(users.create(request));
    }

    @PutMapping("/{id}")
    public UsuarioResponse update(@PathVariable Integer id, @Valid @RequestBody UsuarioUpdateRequest request,
            Authentication authentication) {
        return users.update(id, request, authentication.getName());
    }

    @PatchMapping("/{id}/password")
    public UsuarioResponse updatePassword(@PathVariable Integer id, @Valid @RequestBody PasswordUpdateRequest request) {
        return users.updatePassword(id, request);
    }

    @PatchMapping("/{id}/estado")
    public UsuarioResponse updateStatus(@PathVariable Integer id, @Valid @RequestBody UsuarioEstadoRequest request,
            Authentication authentication) {
        return users.updateStatus(id, request, authentication.getName());
    }
}
