package isw2_G5.liga_amateur.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import isw2_G5.liga_amateur.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Escribe errores de seguridad antes de que la solicitud llegue a los controladores MVC.
 * Reutiliza el mismo DTO que el manejador central de excepciones.
 */
@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Devuelve el estado de acceso correspondiente sin filtrar detalles internos al cliente.
     *
     * @param request solicitud rechazada
     * @param response respuesta HTTP a escribir
     * @param status 401 o 403
     * @throws IOException si no puede escribirse la respuesta
     */
    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status) throws IOException {
        String message = status == HttpStatus.UNAUTHORIZED ? "Autenticación requerida" : "Acceso denegado";
        ApiErrorResponse error = new ApiErrorResponse(
                status.value(), message, Instant.now(), request.getRequestURI(), List.of());
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), error);
    }
}
