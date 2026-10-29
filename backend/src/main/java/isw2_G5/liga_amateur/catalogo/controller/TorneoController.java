package isw2_G5.liga_amateur.catalogo.controller;

import isw2_G5.liga_amateur.catalogo.dto.TorneoResponse;
import isw2_G5.liga_amateur.catalogo.service.TorneoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Consulta pública de torneos (RF-05). */
@RestController
@RequestMapping("/api/torneos")
public class TorneoController {

    private final TorneoService torneoService;

    public TorneoController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    /**
     * Lista los torneos activos.
     *
     * @return torneos activos
     */
    @GetMapping
    public List<TorneoResponse> listar() {
        return torneoService.listar();
    }
}
