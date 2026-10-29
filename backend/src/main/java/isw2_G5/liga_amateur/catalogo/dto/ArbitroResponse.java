package isw2_G5.liga_amateur.catalogo.dto;

import isw2_G5.liga_amateur.catalogo.entity.Arbitro;

/**
 * Datos públicos de un árbitro según {@code docs/API.md}; evita exponer la entidad JPA.
 *
 * @param id identificador del árbitro
 * @param nombre nombre
 * @param apellido apellido
 * @param dni documento, tratado como texto
 * @param activo si el árbitro está habilitado
 */
public record ArbitroResponse(Integer id, String nombre, String apellido, String dni, boolean activo) {

    /**
     * Convierte la entidad en el DTO de respuesta.
     *
     * @param arbitro entidad leída de la base
     * @return DTO con los campos del contrato
     */
    public static ArbitroResponse from(Arbitro arbitro) {
        return new ArbitroResponse(arbitro.getId(), arbitro.getNombre(), arbitro.getApellido(),
                arbitro.getDni(), arbitro.isActivo());
    }
}