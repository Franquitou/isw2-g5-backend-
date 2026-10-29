package isw2_G5.liga_amateur.catalogo.dto;

import isw2_G5.liga_amateur.catalogo.entity.Arbitro;

/**
 * Árbitro tal como lo expone la API (RF-05).
 *
 * @param id identificador del árbitro
 * @param nombre nombre del árbitro
 * @param apellido apellido del árbitro
 * @param dni documento del árbitro
 * @param activo indica si el árbitro está habilitado
 */
public record ArbitroResponse(Integer id, String nombre, String apellido, String dni, boolean activo) {

    /**
     * Convierte la entidad en su representación pública.
     *
     * @param arbitro entidad de origen
     * @return DTO con los campos del contrato
     */
    public static ArbitroResponse from(Arbitro arbitro) {
        return new ArbitroResponse(
                arbitro.getId(), arbitro.getNombre(), arbitro.getApellido(), arbitro.getDni(), arbitro.isActivo());
    }
}
