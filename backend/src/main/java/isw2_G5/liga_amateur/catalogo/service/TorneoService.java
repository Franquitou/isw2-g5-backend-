package isw2_G5.liga_amateur.catalogo.service;

import isw2_G5.liga_amateur.catalogo.dto.TorneoResponse;
import isw2_G5.liga_amateur.catalogo.repository.TorneoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta pública de torneos (RF-05). */
@Service
@Transactional(readOnly = true)
public class TorneoService {

    private final TorneoRepository torneoRepository;

    public TorneoService(TorneoRepository torneoRepository) {
        this.torneoRepository = torneoRepository;
    }

    /**
     * Lista los torneos activos.
     *
     * @return torneos activos ordenados por año y nombre
     */
    public List<TorneoResponse> listar() {
        return torneoRepository.findByActivoTrueOrderByAnioDescNombreAsc().stream()
                .map(TorneoResponse::from)
                .toList();
    }
}
