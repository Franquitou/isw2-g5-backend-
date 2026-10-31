package isw2_G5.liga_amateur.partido.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import isw2_G5.liga_amateur.partido.entity.EstadoPartido;
import isw2_G5.liga_amateur.partido.entity.Partido;
import isw2_G5.liga_amateur.partido.service.HorarioPartido;
import java.time.OffsetDateTime;

/**
 * Partido tal como lo expone la API ({@code docs/API.md} §6).
 * {@code fechaHoraInicio} se responde siempre con segundos y offset, por ejemplo {@code 2026-11-07T18:00:00-03:00}.
 *
 * @param id identificador del partido
 * @param torneoId torneo del partido
 * @param numeroFecha jornada
 * @param equipoLocalId equipo local
 * @param equipoVisitanteId equipo visitante
 * @param canchaId cancha asignada
 * @param arbitroId árbitro asignado
 * @param fechaHoraInicio inicio con el offset de la liga
 * @param estado estado actual
 */
public record PartidoResponse(
        Integer id,
        Integer torneoId,
        Integer numeroFecha,
        Integer equipoLocalId,
        Integer equipoVisitanteId,
        Integer canchaId,
        Integer arbitroId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime fechaHoraInicio,
        EstadoPartido estado
) {

    /**
     * Convierte la entidad en su representación pública.
     *
     * @param partido entidad de origen
     * @return DTO con los campos del contrato
     */
    public static PartidoResponse from(Partido partido) {
        return new PartidoResponse(partido.getId(), partido.getTorneoId(), partido.getNumeroFecha(),
                partido.getEquipoLocalId(), partido.getEquipoVisitanteId(), partido.getCanchaId(),
                partido.getArbitroId(), HorarioPartido.conZonaLiga(partido.getFechaHoraInicio()),
                partido.getEstado());
    }
}