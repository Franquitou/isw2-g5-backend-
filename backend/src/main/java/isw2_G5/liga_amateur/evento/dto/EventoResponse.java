package isw2_G5.liga_amateur.evento.dto;

import isw2_G5.liga_amateur.evento.entity.EventoPartido;
import isw2_G5.liga_amateur.evento.entity.TipoEvento;

/**
 * Evento de un partido tal como lo expone la API.
 *
 * @param id identificador del evento
 * @param partidoId partido al que pertenece
 * @param jugadorId jugador protagonista
 * @param tipo gol, amarilla o roja
 * @param minuto minuto del evento
 */
public record EventoResponse(Integer id, Integer partidoId, Integer jugadorId, TipoEvento tipo, Integer minuto) {

    /** Convierte la entidad en su representación pública. */
    public static EventoResponse from(EventoPartido evento) {
        return new EventoResponse(evento.getId(), evento.getPartidoId(), evento.getJugadorId(),
                evento.getTipo(), evento.getMinuto());
    }
}
