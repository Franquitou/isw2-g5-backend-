package isw2_G5.liga_amateur.partido.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import isw2_G5.liga_amateur.evento.dto.Marcador;
import isw2_G5.liga_amateur.partido.entity.EstadoPartido;
import isw2_G5.liga_amateur.partido.entity.Partido;
import isw2_G5.liga_amateur.partido.service.HorarioPartido;
import java.time.OffsetDateTime;

/**
 * Partido dentro del listado por jornada (RF-10). Agrega al {@link PartidoResponse} el resultado derivado de
 * los eventos, con la misma estructura que usa el detalle de Mateo.
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
 * @param resultado marcador calculado por {@code MarcadorService}; 0-0 sin goles
 */
public record PartidoListadoResponse(
        Integer id,
        Integer torneoId,
        Integer numeroFecha,
        Integer equipoLocalId,
        Integer equipoVisitanteId,
        Integer canchaId,
        Integer arbitroId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime fechaHoraInicio,
        EstadoPartido estado,
        Marcador resultado
) {

    /**
     * Convierte la entidad y su marcador en la fila del listado.
     *
     * @param partido entidad de origen
     * @param resultado marcador ya calculado
     * @return DTO con los campos del contrato
     */
    public static PartidoListadoResponse from(Partido partido, Marcador resultado) {
        return new PartidoListadoResponse(partido.getId(), partido.getTorneoId(), partido.getNumeroFecha(),
                partido.getEquipoLocalId(), partido.getEquipoVisitanteId(), partido.getCanchaId(),
                partido.getArbitroId(), HorarioPartido.conZonaLiga(partido.getFechaHoraInicio()),
                partido.getEstado(), resultado);
    }
}
