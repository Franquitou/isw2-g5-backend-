package isw2_G5.liga_amateur.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Alta administrativa; la contraseña nunca se devuelve en una respuesta. */
public record UsuarioCreateRequest(
        @NotBlank @Size(max = 40) String username,
        @NotBlank @Size(min = 8) String password,
        @NotBlank @Size(max = 100) String nombreCompleto,
        @NotBlank @Pattern(regexp = "ADMIN|DELEGADO") String rol
) {
}
