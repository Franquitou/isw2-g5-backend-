package isw2_G5.liga_amateur.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Solicitud específica de cambio de contraseña por un administrador. */
public record PasswordUpdateRequest(@NotBlank @Size(min = 8) String password) {
}
