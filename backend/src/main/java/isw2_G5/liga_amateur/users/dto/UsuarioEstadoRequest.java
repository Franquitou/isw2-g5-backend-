package isw2_G5.liga_amateur.users.dto;

import jakarta.validation.constraints.NotNull;

/** Solicitud específica de desactivación o reactivación lógica. */
public record UsuarioEstadoRequest(@NotNull Boolean activo) {
}
