package isw2_G5.liga_amateur.common.error;

/**
 * Indica que la operación entra en conflicto con datos ya existentes o con una
 * restricción de unicidad. El manejador global la traduce a HTTP 409 con el
 * cuerpo de error común.
 */
public class ConflictoException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje apto para el cliente.
     *
     * @param message mensaje que se devuelve en el cuerpo del error
     */
    public ConflictoException(String message) {
        super(message);
    }
}
