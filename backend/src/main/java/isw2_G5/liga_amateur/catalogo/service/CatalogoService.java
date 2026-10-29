package isw2_G5.liga_amateur.catalogo.service;

import isw2_G5.liga_amateur.catalogo.dto.ArbitroResponse;
import isw2_G5.liga_amateur.catalogo.dto.CanchaResponse;
import isw2_G5.liga_amateur.catalogo.dto.EquipoResponse;
import isw2_G5.liga_amateur.catalogo.dto.JugadorResponse;
import isw2_G5.liga_amateur.catalogo.dto.TorneoResponse;
import isw2_G5.liga_amateur.catalogo.repository.ArbitroRepository;
import isw2_G5.liga_amateur.catalogo.repository.CanchaRepository;
import isw2_G5.liga_amateur.catalogo.repository.EquipoRepository;
import isw2_G5.liga_amateur.catalogo.repository.JugadorRepository;
import isw2_G5.liga_amateur.catalogo.repository.TorneoRepository;
import isw2_G5.liga_amateur.common.error.RecursoNoEncontradoException;
import isw2_G5.liga_amateur.common.error.SolicitudInvalidaException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consultas públicas del catálogo (RF-05).
 *
 * Solo devuelve registros activos y responde 404 cuando el torneo o el equipo
 * consultado no existe. Los DTOs evitan exponer las entidades JPA.
 */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final TorneoRepository torneoRepository;
    private final CanchaRepository canchaRepository;
    private final ArbitroRepository arbitroRepository;
    private final EquipoRepository equipoRepository;
    private final JugadorRepository jugadorRepository;

    public CatalogoService(
            TorneoRepository torneoRepository,
            CanchaRepository canchaRepository,
            ArbitroRepository arbitroRepository,
            EquipoRepository equipoRepository,
            JugadorRepository jugadorRepository) {
        this.torneoRepository = torneoRepository;
        this.canchaRepository = canchaRepository;
        this.arbitroRepository = arbitroRepository;
        this.equipoRepository = equipoRepository;
        this.jugadorRepository = jugadorRepository;
    }

    /**
     * Lista los torneos activos.
     *
     * @return torneos activos ordenados por año y nombre
     */
    public List<TorneoResponse> listarTorneos() {
        return torneoRepository.findByActivoTrueOrderByAnioDescNombreAsc().stream()
                .map(TorneoResponse::from)
                .toList();
    }

    /**
     * Lista las canchas activas.
     *
     * @return canchas activas ordenadas por nombre
     */
    public List<CanchaResponse> listarCanchas() {
        return canchaRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(CanchaResponse::from)
                .toList();
    }

    /**
     * Lista los árbitros activos.
     *
     * @return árbitros activos ordenados por apellido y nombre
     */
    public List<ArbitroResponse> listarArbitros() {
        return arbitroRepository.findByActivoTrueOrderByApellidoAscNombreAsc().stream()
                .map(ArbitroResponse::from)
                .toList();
    }

    /**
     * Lista los equipos activos de un torneo existente.
     *
     * @param torneoId identificador del torneo
     * @return equipos activos del torneo
     * @throws SolicitudInvalidaException si el identificador no es positivo
     * @throws RecursoNoEncontradoException si el torneo no existe
     */
    public List<EquipoResponse> listarEquiposPorTorneo(Integer torneoId) {
        validarIdPositivo(torneoId, "torneo");
        if (!torneoRepository.existsById(torneoId)) {
            throw new RecursoNoEncontradoException("No existe el torneo con id " + torneoId);
        }
        return equipoRepository.findByTorneoIdAndActivoTrueOrderByNombreAsc(torneoId).stream()
                .map(EquipoResponse::from)
                .toList();
    }

    /**
     * Lista los jugadores activos de un equipo existente.
     *
     * @param equipoId identificador del equipo
     * @return jugadores activos del equipo
     * @throws SolicitudInvalidaException si el identificador no es positivo
     * @throws RecursoNoEncontradoException si el equipo no existe
     */
    public List<JugadorResponse> listarJugadoresPorEquipo(Integer equipoId) {
        validarIdPositivo(equipoId, "equipo");
        if (!equipoRepository.existsById(equipoId)) {
            throw new RecursoNoEncontradoException("No existe el equipo con id " + equipoId);
        }
        return jugadorRepository.findByEquipoIdAndActivoTrueOrderByApellidoAscNombreAsc(equipoId).stream()
                .map(JugadorResponse::from)
                .toList();
    }

    private void validarIdPositivo(Integer id, String recurso) {
        if (id == null || id <= 0) {
            throw new SolicitudInvalidaException("El identificador de " + recurso + " debe ser un entero positivo");
        }
    }
}
