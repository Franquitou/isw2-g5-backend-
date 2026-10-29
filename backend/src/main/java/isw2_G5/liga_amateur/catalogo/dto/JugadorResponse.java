package isw2_G5.liga_amateur.catalogo.dto;

import isw2_G5.liga_amateur.catalogo.entity.Jugador;

/**
 * Jugador tal como lo expone la API (RF-04 y RF-05).
 *
 * @param id identificador del jugador
 * @param equipoId equipo al que pertenece
 * @param nombre nombre del jugador
 * @param apellido apellido del jugador
 * @param dni documento único en la liga
 * @param numeroCamiseta número de camiseta, opcional
 * @param activo indica si el jugador sigue habilitado
 */
public record JugadorResponse(
        Integer id,
        Integer equipoId,
        String nombre,
        String apellido,
        String dni,
        Integer numeroCamiseta,
        boolean activo
) {

    /**
     * Convierte la entidad en su representación pública.
     *
     * @param jugador entidad de origen
     * @return DTO con los campos del contrato
     */
    public static JugadorResponse from(Jugador jugador) {
        return new JugadorResponse(
                jugador.getId(),
                jugador.getEquipoId(),
                jugador.getNombre(),
                jugador.getApellido(),
                jugador.getDni(),
                jugador.getNumeroCamiseta(),
                jugador.isActivo());
    }
}
