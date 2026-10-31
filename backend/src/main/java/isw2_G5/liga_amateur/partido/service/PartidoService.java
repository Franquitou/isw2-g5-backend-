package isw2_G5.liga_amateur.partido.service;

import isw2_G5.liga_amateur.catalogo.entity.Arbitro;
import isw2_G5.liga_amateur.catalogo.entity.Cancha;
import isw2_G5.liga_amateur.catalogo.entity.Equipo;
import isw2_G5.liga_amateur.catalogo.repository.ArbitroRepository;
import isw2_G5.liga_amateur.catalogo.repository.CanchaRepository;
import isw2_G5.liga_amateur.catalogo.repository.EquipoRepository;
import isw2_G5.liga_amateur.catalogo.repository.TorneoRepository;
import isw2_G5.liga_amateur.common.error.ConflictoException;
import isw2_G5.liga_amateur.common.error.RecursoNoEncontradoException;
import isw2_G5.liga_amateur.common.error.SolicitudInvalidaException;
import isw2_G5.liga_amateur.partido.dto.PartidoResponse;
import isw2_G5.liga_amateur.partido.dto.PartidoWriteRequest;
import isw2_G5.liga_amateur.partido.entity.EstadoPartido;
import isw2_G5.liga_amateur.partido.entity.Partido;
import isw2_G5.liga_amateur.partido.repository.PartidoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Programación de partidos (RF-06): alta, reprogramación y baja de partidos {@code PROGRAMADO}.
 *
 * <p>Reglas validadas en el backend (RNF-07):</p>
 * <ul>
 *   <li>Torneo, equipos, cancha y árbitro existentes (404).</li>
 *   <li>Equipos distintos y del mismo torneo del partido (409).</li>
 *   <li>Equipos, cancha y árbitro habilitados al asignarlos (409).</li>
 *   <li>Un equipo no juega dos veces en la misma jornada (409).</li>
 *   <li>Cancha y árbitro sin otro partido a menos de dos horas (RN-01 y RN-02, 409).</li>
 * </ul>
 * <p>Los partidos {@code SUSPENDIDO} no ocupan cancha, árbitro ni jornada: un suspendido no se reprograma,
 * se crea otro partido en su lugar.</p>
 */
@Service
@Transactional(readOnly = true)
public class PartidoService {

    /** Valor de {@code excluirId} cuando no hay partido propio que ignorar (los ids son positivos). */
    private static final int SIN_PARTIDO = 0;

    private final PartidoRepository partidoRepository;
    private final TorneoRepository torneoRepository;
    private final EquipoRepository equipoRepository;
    private final CanchaRepository canchaRepository;
    private final ArbitroRepository arbitroRepository;

    public PartidoService(PartidoRepository partidoRepository, TorneoRepository torneoRepository,
                          EquipoRepository equipoRepository, CanchaRepository canchaRepository,
                          ArbitroRepository arbitroRepository) {
        this.partidoRepository = partidoRepository;
        this.torneoRepository = torneoRepository;
        this.equipoRepository = equipoRepository;
        this.canchaRepository = canchaRepository;
        this.arbitroRepository = arbitroRepository;
    }

    /**
     * Programa un partido nuevo, que nace {@code PROGRAMADO}.
     *
     * @param request datos validados del partido
     * @return el partido creado
     * @throws RecursoNoEncontradoException si alguna referencia no existe
     * @throws ConflictoException si se viola alguna regla de programación
     */
    @Transactional
    public PartidoResponse programar(PartidoWriteRequest request) {
        LocalDateTime inicio = HorarioPartido.aHoraLiga(request.fechaHoraInicio());
        validar(request, inicio, null);
        Partido partido = new Partido(request.torneoId(), request.numeroFecha(), request.equipoLocalId(),
                request.equipoVisitanteId(), request.canchaId(), request.arbitroId(), inicio);
        return PartidoResponse.from(partidoRepository.save(partido));
    }

    /**
     * Reemplaza los datos de un partido que todavía está {@code PROGRAMADO}.
     *
     * @param id identificador del partido
     * @param request datos completos y validados del partido
     * @return el partido actualizado
     * @throws RecursoNoEncontradoException si el partido o alguna referencia no existe
     * @throws ConflictoException si el partido ya no está programado o se viola alguna regla
     */
    @Transactional
    public PartidoResponse reprogramar(Integer id, PartidoWriteRequest request) {
        Partido partido = buscarProgramado(id, "modificar");
        LocalDateTime inicio = HorarioPartido.aHoraLiga(request.fechaHoraInicio());
        validar(request, inicio, partido);
        partido.reprogramar(request.torneoId(), request.numeroFecha(), request.equipoLocalId(),
                request.equipoVisitanteId(), request.canchaId(), request.arbitroId(), inicio);
        return PartidoResponse.from(partido);
    }

    /**
     * Borra físicamente un partido que todavía está {@code PROGRAMADO}; aún no tiene eventos ni resultado.
     *
     * @param id identificador del partido
     * @throws RecursoNoEncontradoException si el partido no existe
     * @throws ConflictoException si el partido ya no está programado
     */
    @Transactional
    public void eliminar(Integer id) {
        partidoRepository.delete(buscarProgramado(id, "eliminar"));
    }

    private Partido buscarProgramado(Integer id, String accion) {
        if (id == null || id <= 0) {
            throw new SolicitudInvalidaException("El identificador de partido debe ser un entero positivo");
        }
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el partido con id " + id));
        if (partido.getEstado() != EstadoPartido.PROGRAMADO) {
            throw new ConflictoException("Solo se puede " + accion + " un partido PROGRAMADO; este está "
                    + partido.getEstado());
        }
        return partido;
    }

    /**
     * Valida todas las reglas de RF-06. Primero las referencias (404) y después los conflictos (409).
     *
     * @param request datos recibidos
     * @param inicio inicio ya convertido a la zona de la liga
     * @param actual partido que se está editando, o {@code null} al crear
     */
    private void validar(PartidoWriteRequest request, LocalDateTime inicio, Partido actual) {
        if (!torneoRepository.existsById(request.torneoId())) {
            throw new RecursoNoEncontradoException("No existe el torneo con id " + request.torneoId());
        }
        Equipo local = buscarEquipo(request.equipoLocalId());
        Equipo visitante = buscarEquipo(request.equipoVisitanteId());
        Cancha cancha = canchaRepository.findById(request.canchaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cancha con id " + request.canchaId()));
        Arbitro arbitro = arbitroRepository.findById(request.arbitroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el árbitro con id " + request.arbitroId()));

        if (local.getId().equals(visitante.getId())) {
            throw new ConflictoException("El equipo local y el visitante deben ser distintos");
        }
        if (!local.getTorneoId().equals(request.torneoId()) || !visitante.getTorneoId().equals(request.torneoId())) {
            throw new ConflictoException("Ambos equipos deben pertenecer al torneo del partido");
        }
        exigirHabilitado(local.isActivo(), cambia(actual, Partido::getEquipoLocalId, local.getId()),
                "El equipo local está dado de baja");
        exigirHabilitado(visitante.isActivo(), cambia(actual, Partido::getEquipoVisitanteId, visitante.getId()),
                "El equipo visitante está dado de baja");
        exigirHabilitado(cancha.isActivo(), cambia(actual, Partido::getCanchaId, cancha.getId()),
                "La cancha no está habilitada");
        exigirHabilitado(arbitro.isActivo(), cambia(actual, Partido::getArbitroId, arbitro.getId()),
                "El árbitro no está habilitado");

        int excluirId = actual == null ? SIN_PARTIDO : actual.getId();
        long partidosEnFecha = partidoRepository.contarPartidosDeEquiposEnFecha(request.torneoId(),
                request.numeroFecha(), List.of(local.getId(), visitante.getId()), EstadoPartido.SUSPENDIDO,
                excluirId);
        if (partidosEnFecha > 0) {
            throw new ConflictoException("Alguno de los equipos ya tiene un partido en la fecha "
                    + request.numeroFecha());
        }

        LocalDateTime desde = inicio.minus(HorarioPartido.DURACION_OCUPACION);
        LocalDateTime hasta = inicio.plus(HorarioPartido.DURACION_OCUPACION);
        if (hayChoque(partidoRepository.findByCanchaIdAndEstadoNotAndFechaHoraInicioBetween(
                cancha.getId(), EstadoPartido.SUSPENDIDO, desde, hasta), inicio, excluirId)) {
            throw new ConflictoException("La cancha ya tiene otro partido a menos de dos horas de ese horario");
        }
        if (hayChoque(partidoRepository.findByArbitroIdAndEstadoNotAndFechaHoraInicioBetween(
                arbitro.getId(), EstadoPartido.SUSPENDIDO, desde, hasta), inicio, excluirId)) {
            throw new ConflictoException("El árbitro ya tiene otro partido a menos de dos horas de ese horario");
        }
    }

    private Equipo buscarEquipo(Integer id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el equipo con id " + id));
    }

    /**
     * Un equipo, cancha o árbitro dado de baja no puede asignarse. Al reprogramar se permite conservar
     * una asignación que ya tenía el partido: qué hacer con esos partidos es la pregunta abierta P-06.
     */
    private static void exigirHabilitado(boolean activo, boolean seAsigna, String mensaje) {
        if (!activo && seAsigna) {
            throw new ConflictoException(mensaje);
        }
    }

    private static boolean cambia(Partido actual, Function<Partido, Integer> campo, Integer nuevoId) {
        return actual == null || !Objects.equals(campo.apply(actual), nuevoId);
    }

    private static boolean hayChoque(List<Partido> candidatos, LocalDateTime inicio, int excluirId) {
        return candidatos.stream()
                .filter(otro -> otro.getId() != excluirId)
                .anyMatch(otro -> HorarioPartido.chocan(inicio, otro.getFechaHoraInicio()));
    }
}
