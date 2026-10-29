package isw2_G5.liga_amateur.catalogo.web;

import isw2_G5.liga_amateur.catalogo.dto.ArbitroResponse;
import isw2_G5.liga_amateur.catalogo.service.CatalogoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Consulta pública de árbitros (RF-05). */
@RestController
@RequestMapping("/api/arbitros")
public class ArbitroController {

    private final CatalogoService catalogoService;

    public ArbitroController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    /**
     * Lista los árbitros activos.
     *
     * @return árbitros activos
     */
    @GetMapping
    public List<ArbitroResponse> listar() {
        return catalogoService.listarArbitros();
    }
}
