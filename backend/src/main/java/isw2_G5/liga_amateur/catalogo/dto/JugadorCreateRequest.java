package isw2_G5.liga_amateur.catalogo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear un jugador dentro de un equipo (RF-04).
 * El equipo se toma de la ruta, no del cuerpo.
 *
 * @param nombre nombre obligatorio, máximo 60 caracteres
 * @param apellido apellido obligatorio, máximo 60 caracteres
 * @param dni documento obligatorio y único, máximo 10 caracteres
 * @param numeroCamiseta número opcional entre 1 y 99
 */
public record JugadorCreateRequest(
        @NotBlank(message = "no debe estar vacío")
        @Size(max = 60, message = "no debe superar los 60 caracteres")
        String nombre,

        @NotBlank(message = "no debe estar vacío")
        @Size(max = 60, message = "no debe superar los 60 caracteres")
        String apellido,

        @NotBlank(message = "no debe estar vacío")
        @Size(max = 10, message = "no debe superar los 10 caracteres")
        String dni,

        @Min(value = 1, message = "debe ser mayor o igual a 1")
        @Max(value = 99, message = "debe ser menor o igual a 99")
        Integer numeroCamiseta
) {
}
