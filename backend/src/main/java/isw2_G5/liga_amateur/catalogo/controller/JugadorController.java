package isw2_G5.liga_amateur.catalogo.controller;

import isw2_G5.liga_amateur.catalogo.dto.JugadorCreateRequest;
import isw2_G5.liga_amateur.catalogo.dto.JugadorResponse;
import isw2_G5.liga_amateur.catalogo.dto.JugadorUpdateRequest;
import isw2_G5.liga_amateur.catalogo.service.JugadorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta pública y gestión de jugadores (RF-04 y RF-05).
 * Las escrituras quedan restringidas a ADMIN por {@code SecurityConfiguration}.
 */
@RestController
public class JugadorController {

    private final JugadorService jugadorService;

    public JugadorController(JugadorService jugadorService) {
        this.jugadorService = jugadorService;
    }

    /**
     * Lista los jugadores activos de un equipo.
     *
     * @param equipoId identificador del equipo
     * @return jugadores activos del equipo
     */
    @GetMapping("/api/equipos/{equipoId}/jugadores")
    public List<JugadorResponse> listarPorEquipo(@PathVariable Integer equipoId) {
        return jugadorService.listarPorEquipo(equipoId);
    }

    /**
     * Crea un jugador dentro de un equipo.
     *
     * @param equipoId identificador del equipo
     * @param request datos validados del jugador
     * @return el jugador creado
     */
    @PostMapping("/api/equipos/{equipoId}/jugadores")
    @ResponseStatus(HttpStatus.CREATED)
    public JugadorResponse crear(@PathVariable Integer equipoId, @Valid @RequestBody JugadorCreateRequest request) {
        return jugadorService.crear(equipoId, request);
    }

    /**
     * Actualiza un jugador sin cambiar de equipo.
     *
     * @param id identificador del jugador
     * @param request datos validados del jugador
     * @return el jugador actualizado
     */
    @PutMapping("/api/jugadores/{id}")
    public JugadorResponse actualizar(@PathVariable Integer id, @Valid @RequestBody JugadorUpdateRequest request) {
        return jugadorService.actualizar(id, request);
    }

    /**
     * Aplica la baja lógica de un jugador.
     *
     * @param id identificador del jugador
     */
    @DeleteMapping("/api/jugadores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@PathVariable Integer id) {
        jugadorService.darDeBaja(id);
    }
}
