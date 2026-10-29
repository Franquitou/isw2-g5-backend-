package isw2_G5.liga_amateur.catalogo.controller;

import isw2_G5.liga_amateur.catalogo.dto.ArbitroResponse;
import isw2_G5.liga_amateur.catalogo.dto.CanchaResponse;
import isw2_G5.liga_amateur.catalogo.service.CatalogoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints públicos de consulta de canchas y árbitros ({@code docs/API.md}). */
@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    /**
     * {@code GET /api/canchas}: público.
     *
     * @return canchas activas
     */
    @GetMapping("/canchas")
    public List<CanchaResponse> listarCanchas() {
        return catalogoService.listarCanchasActivas();
    }

    /**
     * {@code GET /api/arbitros}: público.
     *
     * @return árbitros activos
     */
    @GetMapping("/arbitros")
    public List<ArbitroResponse> listarArbitros() {
        return catalogoService.listarArbitrosActivos();
    }
}