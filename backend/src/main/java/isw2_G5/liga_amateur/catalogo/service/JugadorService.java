package isw2_G5.liga_amateur.catalogo.service;

import isw2_G5.liga_amateur.catalogo.dto.JugadorCreateRequest;
import isw2_G5.liga_amateur.catalogo.dto.JugadorResponse;
import isw2_G5.liga_amateur.catalogo.dto.JugadorUpdateRequest;
import isw2_G5.liga_amateur.catalogo.entity.Jugador;
import isw2_G5.liga_amateur.catalogo.repository.EquipoRepository;
import isw2_G5.liga_amateur.catalogo.repository.JugadorRepository;
import isw2_G5.liga_amateur.common.error.ConflictoException;
import isw2_G5.liga_amateur.common.error.RecursoNoEncontradoException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta pública y gestión de jugadores (RF-04 y RF-05).
 *
 * El DNI es único en toda la liga, el equipo de un jugador existente es
 * inmutable y la baja es lógica (RN-09).
 */
@Service
@Transactional(readOnly = true)
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final EquipoRepository equipoRepository;

    public JugadorService(JugadorRepository jugadorRepository, EquipoRepository equipoRepository) {
        this.jugadorRepository = jugadorRepository;
        this.equipoRepository = equipoRepository;
    }

    /**
     * Lista los jugadores activos de un equipo existente.
     *
     * @param equipoId identificador del equipo
     * @return jugadores activos ordenados por apellido y nombre
     * @throws isw2_G5.liga_amateur.common.error.SolicitudInvalidaException si el id no es positivo
     * @throws RecursoNoEncontradoException si el equipo no existe
     */
    public List<JugadorResponse> listarPorEquipo(Integer equipoId) {
        Identificadores.requerirPositivo(equipoId, "equipo");
        if (!equipoRepository.existsById(equipoId)) {
            throw new RecursoNoEncontradoException("No existe el equipo con id " + equipoId);
        }
        return jugadorRepository.findByEquipoIdAndActivoTrueOrderByApellidoAscNombreAsc(equipoId).stream()
                .map(JugadorResponse::from)
                .toList();
    }

    /**
     * Crea un jugador activo dentro de un equipo existente.
     *
     * @param equipoId identificador del equipo
     * @param request datos validados del jugador
     * @return el jugador creado
     * @throws RecursoNoEncontradoException si el equipo no existe
     * @throws ConflictoException si el DNI ya está registrado
     */
    @Transactional
    public JugadorResponse crear(Integer equipoId, JugadorCreateRequest request) {
        Identificadores.requerirPositivo(equipoId, "equipo");
        if (!equipoRepository.existsById(equipoId)) {
            throw new RecursoNoEncontradoException("No existe el equipo con id " + equipoId);
        }
        String dni = Identificadores.recortar(request.dni());
        if (jugadorRepository.existsByDni(dni)) {
            throw new ConflictoException("Ya existe un jugador con ese DNI");
        }
        Jugador jugador = new Jugador(
                equipoId,
                Identificadores.recortar(request.nombre()),
                Identificadores.recortar(request.apellido()),
                dni,
                request.numeroCamiseta());
        return JugadorResponse.from(jugadorRepository.save(jugador));
    }

    /**
     * Actualiza los datos editables de un jugador sin cambiar de equipo.
     *
     * @param id identificador del jugador
     * @param request datos validados del jugador
     * @return el jugador actualizado
     * @throws RecursoNoEncontradoException si el jugador no existe
     * @throws ConflictoException si otro jugador ya usa ese DNI
     */
    @Transactional
    public JugadorResponse actualizar(Integer id, JugadorUpdateRequest request) {
        Identificadores.requerirPositivo(id, "jugador");
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el jugador con id " + id));
        String dni = Identificadores.recortar(request.dni());
        if (jugadorRepository.existsByDniAndIdNot(dni, id)) {
            throw new ConflictoException("Ya existe un jugador con ese DNI");
        }
        jugador.actualizar(
                Identificadores.recortar(request.nombre()),
                Identificadores.recortar(request.apellido()),
                dni,
                request.numeroCamiseta());
        return JugadorResponse.from(jugador);
    }

    /**
     * Aplica la baja lógica de un jugador.
     *
     * @param id identificador del jugador
     * @throws RecursoNoEncontradoException si el jugador no existe
     */
    @Transactional
    public void darDeBaja(Integer id) {
        Identificadores.requerirPositivo(id, "jugador");
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el jugador con id " + id));
        jugador.darDeBaja();
    }
}
