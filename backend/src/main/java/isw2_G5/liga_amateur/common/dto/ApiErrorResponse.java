package isw2_G5.liga_amateur.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.List;

/**
 * Respuesta compartida para los errores HTTP de la API; no contiene datos internos de la excepción.
 *
 * @param status código de estado HTTP
 * @param message descripción comprensible del error
 * @param timestamp instante UTC en que se generó la respuesta
 * @param path ruta solicitada, si se conoce
 * @param validationErrors errores de validación de la solicitud, o lista vacía
 */
public record ApiErrorResponse(
        int status,
        String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp,
        String path,
        List<ValidationError> validationErrors
) {
    /**
     * Identifica el campo u objeto inválido sin incluir el valor recibido.
     *
     * @param field campo u objeto que no superó la validación
     * @param message motivo de la validación fallida
     */
    public record ValidationError(String field, String message) {
    }
}
