package isw2_G5.liga_amateur.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear un equipo dentro de un torneo (RF-03).
 * El torneo se toma de la ruta, no del cuerpo.
 *
 * @param nombre nombre obligatorio, máximo 80 caracteres
 * @param barrio barrio opcional, máximo 80 caracteres
 */
public record EquipoCreateRequest(
        @NotBlank(message = "no debe estar vacío")
        @Size(max = 80, message = "no debe superar los 80 caracteres")
        String nombre,

        @Size(max = 80, message = "no debe superar los 80 caracteres")
        String barrio
) {
}
