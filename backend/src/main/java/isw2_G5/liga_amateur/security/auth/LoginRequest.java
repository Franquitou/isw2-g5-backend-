package isw2_G5.liga_amateur.security.auth;

import jakarta.validation.constraints.NotBlank;

/** Datos necesarios para autenticar una cuenta existente, sin registro público. */
public record LoginRequest(@NotBlank String username, @NotBlank String password) {
}
