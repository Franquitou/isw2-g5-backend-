package isw2_G5.liga_amateur.catalogo.dto;

import isw2_G5.liga_amateur.catalogo.entity.Equipo;

/**
 * Equipo tal como lo expone la API (RF-03 y RF-05).
 *
 * @param id identificador del equipo
 * @param torneoId torneo al que pertenece
 * @param nombre nombre del equipo
 * @param barrio barrio del equipo, opcional
 * @param activo indica si el equipo sigue habilitado
 */
public record EquipoResponse(Integer id, Integer torneoId, String nombre, String barrio, boolean activo) {

    /**
     * Convierte la entidad en su representación pública.
     *
     * @param equipo entidad de origen
     * @return DTO con los campos del contrato
     */
    public static EquipoResponse from(Equipo equipo) {
        return new EquipoResponse(
                equipo.getId(), equipo.getTorneoId(), equipo.getNombre(), equipo.getBarrio(), equipo.isActivo());
    }
}
