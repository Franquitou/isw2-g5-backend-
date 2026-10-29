package isw2_G5.liga_amateur.catalogo.web;

import isw2_G5.liga_amateur.catalogo.dto.EquipoResponse;
import isw2_G5.liga_amateur.catalogo.service.CatalogoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Consulta pública de los equipos de un torneo (RF-05). */
@RestController
public class EquipoController {

    private final CatalogoService catalogoService;

    public EquipoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    /**
     * Lista los equipos activos de un torneo.
     *
     * @param torneoId identificador del torneo
     * @return equipos activos del torneo
     */
    @GetMapping("/api/torneos/{torneoId}/equipos")
    public List<EquipoResponse> listarPorTorneo(@PathVariable Integer torneoId) {
        return catalogoService.listarEquiposPorTorneo(torneoId);
    }
}
