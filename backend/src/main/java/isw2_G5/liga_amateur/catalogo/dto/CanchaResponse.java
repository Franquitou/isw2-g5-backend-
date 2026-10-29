package isw2_G5.liga_amateur.catalogo.dto;

import isw2_G5.liga_amateur.catalogo.entity.Cancha;

/**
 * Datos públicos de una cancha según {@code docs/API.md}; evita exponer la entidad JPA.
 *
 * @param id identificador de la cancha
 * @param nombre nombre único
 * @param direccion dirección, puede ser {@code null}
 * @param activo si la cancha está habilitada
 */
public record CanchaResponse(Integer id, String nombre, String direccion, boolean activo) {

    /**
     * Convierte la entidad en el DTO de respuesta.
     *
     * @param cancha entidad leída de la base
     * @return DTO con los campos del contrato
     */
    public static CanchaResponse from(Cancha cancha) {
        return new CanchaResponse(cancha.getId(), cancha.getNombre(), cancha.getDireccion(), cancha.isActivo());
    }
}