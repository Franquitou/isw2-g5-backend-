package isw2_G5.liga_amateur.partido.dto;

import isw2_G5.liga_amateur.partido.entity.EstadoPartido;
import jakarta.validation.constraints.NotNull;

/**
 * Estado de destino para {@code PATCH /api/partidos/{id}/estado} (RF-07).
 * Un valor que no sea {@code PROGRAMADO}, {@code EN_JUEGO}, {@code FINALIZADO} o {@code SUSPENDIDO}
 * no se puede leer y se responde 400.
 *
 * @param estado estado al que se quiere pasar
 */
public record PartidoEstadoRequest(
        @NotNull(message = "es obligatorio")
        EstadoPartido estado
) {
}