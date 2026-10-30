package isw2_G5.liga_amateur.evento.controller;

import isw2_G5.liga_amateur.evento.dto.PartidoDetalleResponse;
import isw2_G5.liga_amateur.evento.dto.PosicionResponse;
import isw2_G5.liga_amateur.evento.dto.SuspendidoResponse;
import isw2_G5.liga_amateur.evento.service.PartidoDetalleService;
import isw2_G5.liga_amateur.evento.service.SuspensionService;
import isw2_G5.liga_amateur.evento.service.TablaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consultas públicas de detalle de partido, tabla de posiciones y suspendidos (RF-10, RF-11, RF-12). */
@RestController
public class ResultadosController {

    private final PartidoDetalleService detalleService;
    private final TablaService tablaService;
    private final SuspensionService suspensionService;

    public ResultadosController(PartidoDetalleService detalleService, TablaService tablaService,
            SuspensionService suspensionService) {
        this.detalleService = detalleService;
        this.tablaService = tablaService;
        this.suspensionService = suspensionService;
    }

    /**
     * Detalle de un partido con resultado y eventos.
     *
     * @param id identificador del partido
     * @return detalle público
     */
    @GetMapping("/api/partidos/{id}")
    public PartidoDetalleResponse detalle(@PathVariable Integer id) {
        return detalleService.detalle(id);
    }

    /**
     * Tabla de posiciones de un torneo.
     *
     * @param torneoId identificador del torneo
     * @return filas ordenadas
     */
    @GetMapping("/api/torneos/{torneoId}/tabla")
    public List<PosicionResponse> tabla(@PathVariable Integer torneoId) {
        return tablaService.tabla(torneoId);
    }

    /**
     * Jugadores suspendidos en una jornada.
     *
     * @param torneoId identificador del torneo
     * @param numeroFecha jornada consultada, obligatoria
     * @return suspendidos de la jornada
     */
    @GetMapping("/api/torneos/{torneoId}/suspendidos")
    public List<SuspendidoResponse> suspendidos(@PathVariable Integer torneoId,
            @RequestParam Integer numeroFecha) {
        return suspensionService.listar(torneoId, numeroFecha);
    }
}
