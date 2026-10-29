package isw2_G5.liga_amateur.catalogo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para actualizar un jugador (RF-04). No incluye {@code equipoId}:
 * el equipo de un jugador existente es inmutable.
 *
 * @param nombre nombre obligatorio, máximo 60 caracteres
 * @param apellido apellido obligatorio, máximo 60 caracteres
 * @param dni documento obligatorio y único, máximo 10 caracteres
 * @param numeroCamiseta número incluido en la actualización; puede ser {@code null}
 */
public record JugadorUpdateRequest(
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
