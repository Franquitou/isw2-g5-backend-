package isw2_G5.liga_amateur.evento.dto;

import isw2_G5.liga_amateur.evento.entity.MotivoSuspension;

/**
 * Jugador suspendido para una jornada (RF-12).
 *
 * @param jugadorId jugador suspendido
 * @param equipoId equipo del jugador
 * @param numeroFecha jornada consultada, en la que cumple la suspensión
 * @param motivo roja o doble amarilla
 * @param numeroFechaSancion jornada N-1 que produjo la sanción
 */
public record SuspendidoResponse(Integer jugadorId, Integer equipoId, int numeroFecha, MotivoSuspension motivo,
        int numeroFechaSancion) {
}
