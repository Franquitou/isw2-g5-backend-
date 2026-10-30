package isw2_G5.liga_amateur.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Campos completos exigidos en PUT; el username inmutable no forma parte del DTO. */
public record UsuarioUpdateRequest(
        @NotBlank @Size(max = 100) String nombreCompleto,
        @NotBlank @Pattern(regexp = "ADMIN|DELEGADO") String rol
) {
}
