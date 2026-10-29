package isw2_G5.liga_amateur.catalogo.web;

import isw2_G5.liga_amateur.catalogo.dto.TorneoResponse;
import isw2_G5.liga_amateur.catalogo.service.CatalogoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Consulta pública de torneos (RF-05). */
@RestController
@RequestMapping("/api/torneos")
public class TorneoController {

    private final CatalogoService catalogoService;

    public TorneoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    /**
     * Lista los torneos activos.
     *
     * @return torneos activos
     */
    @GetMapping
    public List<TorneoResponse> listar() {
        return catalogoService.listarTorneos();
    }
}
