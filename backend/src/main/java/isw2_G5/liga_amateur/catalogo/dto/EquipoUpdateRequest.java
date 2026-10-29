package isw2_G5.liga_amateur.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para actualizar un equipo (RF-03). No permite cambiar de torneo.
 *
 * @param nombre nombre obligatorio, máximo 80 caracteres
 * @param barrio barrio incluido en la actualización; puede ser {@code null}
 */
public record EquipoUpdateRequest(
        @NotBlank(message = "no debe estar vacío")
        @Size(max = 80, message = "no debe superar los 80 caracteres")
        String nombre,

        @Size(max = 80, message = "no debe superar los 80 caracteres")
        String barrio
) {
}
