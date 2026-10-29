package isw2_G5.liga_amateur.catalogo.service;

import isw2_G5.liga_amateur.catalogo.dto.EquipoCreateRequest;
import isw2_G5.liga_amateur.catalogo.dto.EquipoResponse;
import isw2_G5.liga_amateur.catalogo.dto.EquipoUpdateRequest;
import isw2_G5.liga_amateur.catalogo.entity.Equipo;
import isw2_G5.liga_amateur.catalogo.repository.EquipoRepository;
import isw2_G5.liga_amateur.catalogo.repository.TorneoRepository;
import isw2_G5.liga_amateur.common.error.ConflictoException;
import isw2_G5.liga_amateur.common.error.RecursoNoEncontradoException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta pública y gestión de equipos (RF-03 y RF-05).
 *
 * El nombre es único dentro del torneo y la baja es lógica: nunca se borra el
 * historial de un equipo (RN-09).
 */
@Service
@Transactional(readOnly = true)
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final TorneoRepository torneoRepository;

    public EquipoService(EquipoRepository equipoRepository, TorneoRepository torneoRepository) {
        this.equipoRepository = equipoRepository;
        this.torneoRepository = torneoRepository;
    }

    /**
     * Lista los equipos activos de un torneo existente.
     *
     * @param torneoId identificador del torneo
     * @return equipos activos ordenados por nombre
     * @throws isw2_G5.liga_amateur.common.error.SolicitudInvalidaException si el id no es positivo
     * @throws RecursoNoEncontradoException si el torneo no existe
     */
    public List<EquipoResponse> listarPorTorneo(Integer torneoId) {
        Identificadores.requerirPositivo(torneoId, "torneo");
        if (!torneoRepository.existsById(torneoId)) {
            throw new RecursoNoEncontradoException("No existe el torneo con id " + torneoId);
        }
        return equipoRepository.findByTorneoIdAndActivoTrueOrderByNombreAsc(torneoId).stream()
                .map(EquipoResponse::from)
                .toList();
    }

    /**
     * Crea un equipo activo dentro de un torneo existente.
     *
     * @param torneoId identificador del torneo
     * @param request datos validados del equipo
     * @return el equipo creado
     * @throws RecursoNoEncontradoException si el torneo no existe
     * @throws ConflictoException si el nombre ya está usado en el torneo
     */
    @Transactional
    public EquipoResponse crear(Integer torneoId, EquipoCreateRequest request) {
        Identificadores.requerirPositivo(torneoId, "torneo");
        if (!torneoRepository.existsById(torneoId)) {
            throw new RecursoNoEncontradoException("No existe el torneo con id " + torneoId);
        }
        String nombre = Identificadores.recortar(request.nombre());
        if (equipoRepository.existsByTorneoIdAndNombre(torneoId, nombre)) {
            throw new ConflictoException("Ya existe un equipo con ese nombre en el torneo");
        }
        Equipo equipo = new Equipo(torneoId, nombre, Identificadores.recortarOpcional(request.barrio()));
        return EquipoResponse.from(equipoRepository.save(equipo));
    }

    /**
     * Actualiza los datos editables de un equipo sin cambiar de torneo.
     *
     * @param id identificador del equipo
     * @param request datos validados del equipo
     * @return el equipo actualizado
     * @throws RecursoNoEncontradoException si el equipo no existe
     * @throws ConflictoException si otro equipo del torneo usa ese nombre
     */
    @Transactional
    public EquipoResponse actualizar(Integer id, EquipoUpdateRequest request) {
        Identificadores.requerirPositivo(id, "equipo");
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el equipo con id " + id));
        String nombre = Identificadores.recortar(request.nombre());
        if (equipoRepository.existsByTorneoIdAndNombreAndIdNot(equipo.getTorneoId(), nombre, id)) {
            throw new ConflictoException("Ya existe un equipo con ese nombre en el torneo");
        }
        equipo.actualizar(nombre, Identificadores.recortarOpcional(request.barrio()));
        return EquipoResponse.from(equipo);
    }

    /**
     * Aplica la baja lógica de un equipo.
     *
     * @param id identificador del equipo
     * @throws RecursoNoEncontradoException si el equipo no existe
     */
    @Transactional
    public void darDeBaja(Integer id) {
        Identificadores.requerirPositivo(id, "equipo");
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el equipo con id " + id));
        equipo.darDeBaja();
    }
}
