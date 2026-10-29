package isw2_G5.liga_amateur.common.error;

/**
 * Indica que la solicitud no cumple una validación que no puede expresarse
 * con anotaciones sobre el cuerpo, por ejemplo un identificador de ruta no positivo.
 * El manejador global la traduce a HTTP 400 con el cuerpo de error común.
 */
public class SolicitudInvalidaException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje apto para el cliente.
     *
     * @param message mensaje que se devuelve en el cuerpo del error
     */
    public SolicitudInvalidaException(String message) {
        super(message);
    }
}
