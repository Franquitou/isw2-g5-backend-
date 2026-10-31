package isw2_G5.liga_amateur.partido.controller;

import isw2_G5.liga_amateur.partido.dto.PartidoEstadoRequest;
import isw2_G5.liga_amateur.partido.dto.PartidoListadoResponse;
import isw2_G5.liga_amateur.partido.dto.PartidoResponse;
import isw2_G5.liga_amateur.partido.dto.PartidoWriteRequest;
import isw2_G5.liga_amateur.partido.service.PartidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Programación (RF-06), cambio de estado (RF-07) y listado por jornada (RF-10) de partidos. Según
 * {@code SecurityConfiguration}, programar, reprogramar y borrar son solo para ADMIN; cambiar el estado,
 * para DELEGADO o ADMIN; y el listado es público.
 */
@RestController
public class PartidoController {

    private final PartidoService partidoService;

    public PartidoController(PartidoService partidoService) {
        this.partidoService = partidoService;
    }

    /**
     * Programa un partido nuevo.
     *
     * @param request datos validados del partido
     * @return el partido creado, en estado {@code PROGRAMADO}
     */
    @PostMapping("/api/partidos")
    @ResponseStatus(HttpStatus.CREATED)
    public PartidoResponse programar(@Valid @RequestBody PartidoWriteRequest request) {
        return partidoService.programar(request);
    }

    /**
     * Reemplaza los datos de un partido {@code PROGRAMADO}.
     *
     * @param id identificador del partido
     * @param request datos completos y validados
     * @return el partido actualizado
     */
    @PutMapping("/api/partidos/{id}")
    public PartidoResponse reprogramar(@PathVariable Integer id, @Valid @RequestBody PartidoWriteRequest request) {
        return partidoService.reprogramar(id, request);
    }
    /**
     * Cambia el estado de un partido respetando RN-05 y RN-07.
     *
     * @param id identificador del partido
     * @param request estado de destino
     * @return el partido con su nuevo estado
     */
    @PatchMapping("/api/partidos/{id}/estado")
    public PartidoResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody PartidoEstadoRequest request) {
        return partidoService.cambiarEstado(id, request.estado());
    }

    /**
     * Lista los partidos de una jornada con su resultado derivado.
     *
     * @param torneoId identificador del torneo
     * @param numeroFecha jornada consultada, obligatoria y desde 1
     * @return partidos de la jornada ordenados por inicio
     */
    @GetMapping("/api/torneos/{torneoId}/partidos")
    public List<PartidoListadoResponse> listar(@PathVariable Integer torneoId,
            @RequestParam Integer numeroFecha) {
        return partidoService.listarPorJornada(torneoId, numeroFecha);
    }
    /**
     * Borra un partido {@code PROGRAMADO}.
     *
     * @param id identificador del partido
     */
    @DeleteMapping("/api/partidos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        partidoService.eliminar(id);
    }
}