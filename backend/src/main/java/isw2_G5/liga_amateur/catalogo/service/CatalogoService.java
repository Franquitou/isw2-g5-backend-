package isw2_G5.liga_amateur.catalogo.service;

import isw2_G5.liga_amateur.catalogo.dto.ArbitroResponse;
import isw2_G5.liga_amateur.catalogo.dto.CanchaResponse;
import isw2_G5.liga_amateur.catalogo.repository.ArbitroRepository;
import isw2_G5.liga_amateur.catalogo.repository.CanchaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas públicas de canchas y árbitros activos (datos precargados, sin escritura). */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final CanchaRepository canchaRepository;
    private final ArbitroRepository arbitroRepository;

    public CatalogoService(CanchaRepository canchaRepository, ArbitroRepository arbitroRepository) {
        this.canchaRepository = canchaRepository;
        this.arbitroRepository = arbitroRepository;
    }

    /**
     * Lista las canchas habilitadas.
     *
     * @return canchas activas ordenadas por nombre
     */
    public List<CanchaResponse> listarCanchasActivas() {
        return canchaRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(CanchaResponse::from)
                .toList();
    }

    /**
     * Lista los árbitros habilitados.
     *
     * @return árbitros activos ordenados por apellido y nombre
     */
    public List<ArbitroResponse> listarArbitrosActivos() {
        return arbitroRepository.findByActivoTrueOrderByApellidoAscNombreAsc().stream()
                .map(ArbitroResponse::from)
                .toList();
    }
}