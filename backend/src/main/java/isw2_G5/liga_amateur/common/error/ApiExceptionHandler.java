package isw2_G5.liga_amateur.common.error;

import isw2_G5.liga_amateur.common.dto.ApiErrorResponse;
import isw2_G5.liga_amateur.common.dto.ApiErrorResponse.ValidationError;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Uniforma los errores de Spring MVC y evita revelar detalles internos ante fallos inesperados.
 * Los futuros módulos pueden incorporar sus excepciones específicas sin cambiar el contrato JSON.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Devuelve todos los errores de validación del cuerpo sin exponer los valores enviados.
     *
     * @param exception error generado por la validación del argumento
     * @param headers encabezados de la respuesta
     * @param status estado HTTP asignado por Spring MVC
     * @param request solicitud que originó el error
     * @return respuesta HTTP con los campos inválidos
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        List<ValidationError> errors = exception.getBindingResult().getAllErrors().stream()
                .map(error -> new ValidationError(
                        error instanceof org.springframework.validation.FieldError fieldError
                                ? fieldError.getField() : error.getObjectName(),
                        error.getDefaultMessage() == null ? "Valor inválido" : error.getDefaultMessage()))
                .toList();
        ApiErrorResponse response = response(status, "La solicitud contiene datos inválidos", request, errors);
        return super.handleExceptionInternal(exception, response, headers, status, request);
    }

    /**
     * Conserva el estado HTTP de los errores de Spring MVC con un cuerpo JSON uniforme.
     *
     * @param exception error HTTP de Spring MVC
     * @param body cuerpo preexistente, si lo hay
     * @param headers encabezados de la respuesta
     * @param status estado HTTP asignado por Spring MVC
     * @param request solicitud que originó el error
     * @return respuesta con un mensaje seguro y el estado original
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ApiErrorResponse error = body instanceof ApiErrorResponse apiError
                ? apiError : response(status, messageFor(status), request, List.of());
        return super.handleExceptionInternal(exception, error, headers, status, request);
    }

    /**
     * Oculta excepciones no previstas para no devolver trazas o mensajes internos al cliente.
     * La excepción completa queda registrada en el log del servidor para poder diagnosticarla.
     *
     * @param exception fallo no contemplado por Spring MVC
     * @param request solicitud que originó el error
     * @return respuesta HTTP 500 sin detalles sensibles
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception, WebRequest request) {
        log.error("Error no controlado", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request, List.of()));
    }

    private ApiErrorResponse response(
            HttpStatusCode status, String message, WebRequest request, List<ValidationError> errors) {
        String path = request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI() : null;
        return new ApiErrorResponse(status.value(), message, Instant.now(), path, errors);
    }

    private String messageFor(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Solicitud inválida";
            case 401 -> "Autenticación requerida";
            case 403 -> "Acceso denegado";
            case 404 -> "Recurso no encontrado";
            case 405 -> "Método HTTP no permitido";
            case 415 -> "Tipo de contenido no admitido";
            default -> status.is5xxServerError() ? "Error interno del servidor" : "No se pudo procesar la solicitud";
        };
    }
}
