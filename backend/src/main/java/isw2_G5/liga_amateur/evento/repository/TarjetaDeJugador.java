package isw2_G5.liga_amateur.evento.repository;

import isw2_G5.liga_amateur.evento.entity.TipoEvento;

/**
 * Proyección de una tarjeta de un partido finalizado.
 *
 * @param partidoId partido de la tarjeta
 * @param jugadorId jugador sancionado
 * @param equipoId equipo del jugador
 * @param tipo {@code AMARILLA} o {@code ROJA}
 */
public record TarjetaDeJugador(Integer partidoId, Integer jugadorId, Integer equipoId, TipoEvento tipo) {
}
