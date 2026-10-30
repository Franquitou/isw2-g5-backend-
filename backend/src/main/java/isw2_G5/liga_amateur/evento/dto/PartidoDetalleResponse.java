package isw2_G5.liga_amateur.evento.dto;

import isw2_G5.liga_amateur.partido.entity.EstadoPartido;
import isw2_G5.liga_amateur.partido.entity.Partido;
import isw2_G5.liga_amateur.partido.service.HorarioPartido;
import java.time.OffsetDateTime;
import java.util.List;

/** Detalle público de un partido (RF-10): los campos de {@code PartidoResponse} más resultado y eventos. */
public record PartidoDetalleResponse(
        Integer id,
        Integer torneoId,
        Integer numeroFecha,
        Integer equipoLocalId,
        Integer equipoVisitanteId,
        Integer canchaId,
        Integer arbitroId,
        OffsetDateTime fechaHoraInicio,
        EstadoPartido estado,
        Marcador resultado,
        List<EventoResponse> eventos
) {

    /**
     * Arma el detalle.
     *
     * @param partido partido consultado
     * @param resultado marcador calculado por {@code MarcadorService}
     * @param eventos eventos ordenados por minuto
     * @return detalle listo para serializar
     */
    public static PartidoDetalleResponse from(Partido partido, Marcador resultado, List<EventoResponse> eventos) {
        return new PartidoDetalleResponse(partido.getId(), partido.getTorneoId(), partido.getNumeroFecha(),
                partido.getEquipoLocalId(), partido.getEquipoVisitanteId(), partido.getCanchaId(),
                partido.getArbitroId(), HorarioPartido.conZonaLiga(partido.getFechaHoraInicio()),
                partido.getEstado(), resultado, eventos);
    }
}
