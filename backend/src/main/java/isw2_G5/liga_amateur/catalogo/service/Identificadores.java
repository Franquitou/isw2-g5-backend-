package isw2_G5.liga_amateur.catalogo.service;

import isw2_G5.liga_amateur.common.error.SolicitudInvalidaException;

/** Validaciones de identificadores de ruta compartidas por los servicios del catálogo. */
final class Identificadores {

    private Identificadores() {
    }

    /**
     * Exige que un identificador de ruta sea un entero positivo.
     *
     * @param id identificador recibido
     * @param recurso nombre del recurso, para el mensaje de error
     * @throws SolicitudInvalidaException si el identificador es nulo o no positivo
     */
    static void requerirPositivo(Integer id, String recurso) {
        if (id == null || id <= 0) {
            throw new SolicitudInvalidaException(
                    "El identificador de " + recurso + " debe ser un entero positivo");
        }
    }

    /**
     * Recorta un texto obligatorio ya validado por Bean Validation.
     *
     * @param valor texto recibido
     * @return el texto sin espacios en los extremos
     */
    static String recortar(String valor) {
        return valor.trim();
    }

    /**
     * Recorta un texto opcional y convierte el vacío en {@code null}.
     *
     * @param valor texto recibido, puede ser {@code null}
     * @return el texto sin espacios, o {@code null} si queda vacío
     */
    static String recortarOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String recortado = valor.trim();
        return recortado.isEmpty() ? null : recortado;
    }
}
