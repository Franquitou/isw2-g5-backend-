package isw2_G5.liga_amateur.common.error;

/**
 * Indica que un recurso solicitado por la API no existe.
 * El manejador global la traduce a HTTP 404 con el cuerpo de error común.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje apto para el cliente.
     *
     * @param message mensaje que se devuelve en el cuerpo del error
     */
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
