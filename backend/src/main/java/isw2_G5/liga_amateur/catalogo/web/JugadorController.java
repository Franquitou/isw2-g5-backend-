package isw2_G5.liga_amateur.catalogo.web;

import isw2_G5.liga_amateur.catalogo.dto.JugadorResponse;
import isw2_G5.liga_amateur.catalogo.service.CatalogoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Consulta pública de los jugadores de un equipo (RF-05). */
@RestController
public class JugadorController {

    private final CatalogoService catalogoService;

    public JugadorController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    /**
     * Lista los jugadores activos de un equipo.
     *
     * @param equipoId identificador del equipo
     * @return jugadores activos del equipo
     */
    @GetMapping("/api/equipos/{equipoId}/jugadores")
    public List<JugadorResponse> listarPorEquipo(@PathVariable Integer equipoId) {
        return catalogoService.listarJugadoresPorEquipo(equipoId);
    }
}
