package isw2_G5.liga_amateur.partido.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;

/**
 * Datos para programar o reprogramar un partido (RF-06). No recibe estado ni marcador:
 * el partido nace {@code PROGRAMADO} y su resultado se calcula a partir de los eventos.
 *
 * @param torneoId torneo del partido
 * @param numeroFecha jornada, desde 1
 * @param equipoLocalId equipo local
 * @param equipoVisitanteId equipo visitante
 * @param canchaId cancha asignada
 * @param arbitroId árbitro asignado
 * @param fechaHoraInicio inicio en ISO-8601 con offset, por ejemplo {@code 2026-11-07T18:00:00-03:00}
 */
public record PartidoWriteRequest(
        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser un entero positivo")
        Integer torneoId,

        @NotNull(message = "es obligatorio")
        @Min(value = 1, message = "debe ser mayor o igual a 1")
        Integer numeroFecha,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser un entero positivo")
        Integer equipoLocalId,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser un entero positivo")
        Integer equipoVisitanteId,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser un entero positivo")
        Integer canchaId,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser un entero positivo")
        Integer arbitroId,

        @NotNull(message = "es obligatorio")
        OffsetDateTime fechaHoraInicio
) {
}