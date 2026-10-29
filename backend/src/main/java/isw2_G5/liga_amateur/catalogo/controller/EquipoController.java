package isw2_G5.liga_amateur.catalogo.controller;

import isw2_G5.liga_amateur.catalogo.dto.EquipoCreateRequest;
import isw2_G5.liga_amateur.catalogo.dto.EquipoResponse;
import isw2_G5.liga_amateur.catalogo.dto.EquipoUpdateRequest;
import isw2_G5.liga_amateur.catalogo.service.EquipoService;
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
 * Consulta pública y gestión de equipos (RF-03 y RF-05).
 * Las escrituras quedan restringidas a ADMIN por {@code SecurityConfiguration}.
 */
@RestController
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    /**
     * Lista los equipos activos de un torneo.
     *
     * @param torneoId identificador del torneo
     * @return equipos activos del torneo
     */
    @GetMapping("/api/torneos/{torneoId}/equipos")
    public List<EquipoResponse> listarPorTorneo(@PathVariable Integer torneoId) {
        return equipoService.listarPorTorneo(torneoId);
    }

    /**
     * Crea un equipo dentro de un torneo.
     *
     * @param torneoId identificador del torneo
     * @param request datos validados del equipo
     * @return el equipo creado
     */
    @PostMapping("/api/torneos/{torneoId}/equipos")
    @ResponseStatus(HttpStatus.CREATED)
    public EquipoResponse crear(@PathVariable Integer torneoId, @Valid @RequestBody EquipoCreateRequest request) {
        return equipoService.crear(torneoId, request);
    }

    /**
     * Actualiza un equipo sin cambiar de torneo.
     *
     * @param id identificador del equipo
     * @param request datos validados del equipo
     * @return el equipo actualizado
     */
    @PutMapping("/api/equipos/{id}")
    public EquipoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody EquipoUpdateRequest request) {
        return equipoService.actualizar(id, request);
    }

    /**
     * Aplica la baja lógica de un equipo.
     *
     * @param id identificador del equipo
     */
    @DeleteMapping("/api/equipos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@PathVariable Integer id) {
        equipoService.darDeBaja(id);
    }
}
