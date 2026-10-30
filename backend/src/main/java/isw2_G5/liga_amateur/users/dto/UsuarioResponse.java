package isw2_G5.liga_amateur.users.dto;

import java.time.OffsetDateTime;

/** Respuesta pública del módulo administrativo sin contraseña ni hash BCrypt. */
public record UsuarioResponse(
        Integer id,
        String username,
        String nombreCompleto,
        String rol,
        boolean activo,
        OffsetDateTime fechaAlta
) {
}
