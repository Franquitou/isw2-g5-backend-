package isw2_G5.liga_amateur.evento.dto;

import isw2_G5.liga_amateur.evento.entity.TipoEvento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Datos para registrar un evento en un partido en juego (RF-08). El partido viene de la ruta.
 *
 * @param jugadorId jugador protagonista, obligatorio
 * @param tipo {@code GOL}, {@code AMARILLA} o {@code ROJA}
 * @param minuto minuto entero entre 1 y 120
 */
public record EventoCreateRequest(
        @NotNull(message = "es obligatorio")
        Integer jugadorId,

        @NotNull(message = "es obligatorio")
        TipoEvento tipo,

        @NotNull(message = "es obligatorio")
        @Min(value = 1, message = "debe ser mayor o igual a 1")
        @Max(value = 120, message = "debe ser menor o igual a 120")
        Integer minuto
) {
}
