package isw2_G5.liga_amateur.evento.controller;

import isw2_G5.liga_amateur.evento.dto.EventoCreateRequest;
import isw2_G5.liga_amateur.evento.dto.EventoResponse;
import isw2_G5.liga_amateur.evento.service.EventoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro y corrección de eventos (RF-08 y RF-09). Solo ADMIN o DELEGADO, según {@code SecurityConfiguration}.
 */
@RestController
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    /**
     * Registra un evento en un partido en juego.
     *
     * @param partidoId partido de la ruta
     * @param request datos validados del evento
     * @return el evento creado
     */
    @PostMapping("/api/partidos/{partidoId}/eventos")
    @ResponseStatus(HttpStatus.CREATED)
    public EventoResponse crear(@PathVariable Integer partidoId, @Valid @RequestBody EventoCreateRequest request) {
        return eventoService.crear(partidoId, request);
    }

    /**
     * Elimina un evento de un partido en juego.
     *
     * @param partidoId partido de la ruta
     * @param eventoId evento a eliminar
     */
    @DeleteMapping("/api/partidos/{partidoId}/eventos/{eventoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer partidoId, @PathVariable Integer eventoId) {
        eventoService.eliminar(partidoId, eventoId);
    }
}
