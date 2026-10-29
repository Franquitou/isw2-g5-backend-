package isw2_G5.liga_amateur.catalogo.dto;

import isw2_G5.liga_amateur.catalogo.entity.Cancha;

/**
 * Cancha tal como la expone la API (RF-05).
 *
 * @param id identificador de la cancha
 * @param nombre nombre de la cancha
 * @param direccion dirección, opcional
 * @param activo indica si la cancha está habilitada
 */
public record CanchaResponse(Integer id, String nombre, String direccion, boolean activo) {

    /**
     * Convierte la entidad en su representación pública.
     *
     * @param cancha entidad de origen
     * @return DTO con los campos del contrato
     */
    public static CanchaResponse from(Cancha cancha) {
        return new CanchaResponse(cancha.getId(), cancha.getNombre(), cancha.getDireccion(), cancha.isActivo());
    }
}
