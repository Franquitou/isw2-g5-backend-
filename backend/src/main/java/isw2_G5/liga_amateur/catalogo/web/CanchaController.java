package isw2_G5.liga_amateur.catalogo.web;

import isw2_G5.liga_amateur.catalogo.dto.CanchaResponse;
import isw2_G5.liga_amateur.catalogo.service.CatalogoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Consulta pública de canchas (RF-05). */
@RestController
@RequestMapping("/api/canchas")
public class CanchaController {

    private final CatalogoService catalogoService;

    public CanchaController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    /**
     * Lista las canchas activas.
     *
     * @return canchas activas
     */
    @GetMapping
    public List<CanchaResponse> listar() {
        return catalogoService.listarCanchas();
    }
}
