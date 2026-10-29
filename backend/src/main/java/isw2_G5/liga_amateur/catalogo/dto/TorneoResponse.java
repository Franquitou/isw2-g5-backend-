package isw2_G5.liga_amateur.catalogo.dto;

import isw2_G5.liga_amateur.catalogo.entity.Torneo;

/**
 * Torneo tal como lo expone la API (RF-05).
 *
 * @param id identificador del torneo
 * @param nombre nombre del torneo
 * @param anio año de disputa
 * @param activo indica si el torneo está vigente
 */
public record TorneoResponse(Integer id, String nombre, Integer anio, boolean activo) {

    /**
     * Convierte la entidad en su representación pública.
     *
     * @param torneo entidad de origen
     * @return DTO con los campos del contrato
     */
    public static TorneoResponse from(Torneo torneo) {
        return new TorneoResponse(torneo.getId(), torneo.getNombre(), torneo.getAnio(), torneo.isActivo());
    }
}
