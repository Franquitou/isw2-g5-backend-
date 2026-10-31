package isw2_G5.liga_amateur.partido.service;

import isw2_G5.liga_amateur.catalogo.entity.Arbitro;
import isw2_G5.liga_amateur.catalogo.entity.Cancha;
import isw2_G5.liga_amateur.catalogo.entity.Equipo;
import isw2_G5.liga_amateur.catalogo.repository.TorneoRepository;
import isw2_G5.liga_amateur.common.error.ConflictoException;
import isw2_G5.liga_amateur.common.error.RecursoNoEncontradoException;
import isw2_G5.liga_amateur.common.error.SolicitudInvalidaException;
import isw2_G5.liga_amateur.partido.dto.PartidoResponse;
import isw2_G5.liga_amateur.partido.dto.PartidoWriteRequest;
import isw2_G5.liga_amateur.partido.entity.EstadoPartido;
import isw2_G5.liga_amateur.partido.entity.Partido;
import isw2_G5.liga_amateur.partido.repository.PartidoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Programación de partidos (RF-06): alta, reprogramación y baja de partidos {@code PROGRAMADO}.
 *
 * <p>Reglas validadas en el backend (RNF-07):</p>
 * <ul>
 *   <li>Torneo, equipos, cancha y árbitro existentes (404).</li>
 *   <li>Equipos distintos y del mismo torneo del partido (409).</li>
 *   <li>Equipos, cancha y árbitro habilitados, tanto al crear como al reprogramar (409).</li>
 *   <li>Un equipo no juega dos veces en la misma jornada (409).</li>
 *   <li>Cancha y árbitro sin otro partido a menos de dos horas (RN-01 y RN-02, 409).</li>
 * </ul>
 * <p>Los partidos {@code SUSPENDIDO} no ocupan cancha, árbitro ni jornada: un suspendido no se reprograma,
 * se crea otro partido en su lugar.</p>
 *
 * <p><b>Concurrencia.</b> Validar y guardar ocurre en una sola transacción que primero bloquea
 * ({@code PESSIMISTIC_WRITE}) las filas de los equipos, la cancha y el árbitro involucrados. Dos solicitudes
 * que comparten alguno de esos recursos se ejecutan una después de la otra, y la segunda ya ve el partido
 * de la primera, por lo que no pueden generarse superposiciones. Los bloqueos se toman siempre en el mismo
 * orden (equipos por id ascendente, cancha, árbitro) para evitar esperas cruzadas.</p>
 */
@Service
@Transactional(readOnly = true)
public class PartidoService {

    /** Valor de {@code excluirId} cuando no hay partido propio que ignorar (los ids son positivos). */
    private static final int SIN_PARTIDO = 0;

    private final PartidoRepository partidoRepository;
    private final TorneoRepository torneoRepository;
    private final EntityManager entityManager;

    public PartidoService(PartidoRepository partidoRepository, TorneoRepository torneoRepository,
                          EntityManager entityManager) {
        this.partidoRepository = partidoRepository;
        this.torneoRepository = torneoRepository;
        this.entityManager = entityManager;
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
        validar(request, inicio, SIN_PARTIDO);
        Partido partido = new Partido(request.torneoId(), request.numeroFecha(), request.equipoLocalId(),
                request.equipoVisitanteId(), request.canchaId(), request.arbitroId(), inicio);
        return PartidoResponse.from(partidoRepository.saveAndFlush(partido));
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
        validar(request, inicio, partido.getId());
        partido.reprogramar(request.torneoId(), request.numeroFecha(), request.equipoLocalId(),
                request.equipoVisitanteId(), request.canchaId(), request.arbitroId(), inicio);
        partidoRepository.flush();
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

    /** Busca y bloquea el partido, para que dos operaciones sobre el mismo partido no se pisen. */
    private Partido buscarProgramado(Integer id, String accion) {
        if (id == null || id <= 0) {
            throw new SolicitudInvalidaException("El identificador de partido debe ser un entero positivo");
        }
        Partido partido = bloquear(Partido.class, id, () -> "No existe el partido con id " + id);
        if (partido.getEstado() != EstadoPartido.PROGRAMADO) {
            throw new ConflictoException("Solo se puede " + accion + " un partido PROGRAMADO; este está "
                    + partido.getEstado());
        }
        return partido;
    }

    /**
     * Valida todas las reglas de RF-06. Primero las referencias (404) y después los conflictos (409).
     * Las referencias se leen bloqueadas: ver la nota de concurrencia de la clase.
     *
     * @param request datos recibidos
     * @param inicio inicio ya convertido a la zona de la liga
     * @param excluirId partido que se está editando, o {@link #SIN_PARTIDO} al crear
     */
    private void validar(PartidoWriteRequest request, LocalDateTime inicio, int excluirId) {
        if (!torneoRepository.existsById(request.torneoId())) {
            throw new RecursoNoEncontradoException("No existe el torneo con id " + request.torneoId());
        }
        Integer localId = request.equipoLocalId();
        Integer visitanteId = request.equipoVisitanteId();
        Equipo primero = bloquearEquipo(Math.min(localId, visitanteId));
        Equipo segundo = bloquearEquipo(Math.max(localId, visitanteId));
        Equipo local = primero.getId().equals(localId) ? primero : segundo;
        Equipo visitante = primero.getId().equals(visitanteId) ? primero : segundo;
        Cancha cancha = bloquear(Cancha.class, request.canchaId(),
                () -> "No existe la cancha con id " + request.canchaId());
        Arbitro arbitro = bloquear(Arbitro.class, request.arbitroId(),
                () -> "No existe el árbitro con id " + request.arbitroId());

        if (localId.equals(visitanteId)) {
            throw new ConflictoException("El equipo local y el visitante deben ser distintos");
        }
        if (!local.getTorneoId().equals(request.torneoId()) || !visitante.getTorneoId().equals(request.torneoId())) {
            throw new ConflictoException("Ambos equipos deben pertenecer al torneo del partido");
        }
        exigirHabilitado(local.isActivo(), "El equipo local está dado de baja");
        exigirHabilitado(visitante.isActivo(), "El equipo visitante está dado de baja");
        exigirHabilitado(cancha.isActivo(), "La cancha no está habilitada");
        exigirHabilitado(arbitro.isActivo(), "El árbitro no está habilitado");

        long partidosEnFecha = partidoRepository.contarPartidosDeEquiposEnFecha(request.torneoId(),
                request.numeroFecha(), List.of(localId, visitanteId), EstadoPartido.SUSPENDIDO, excluirId);
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

    private Equipo bloquearEquipo(Integer id) {
        return bloquear(Equipo.class, id, () -> "No existe el equipo con id " + id);
    }

    /**
     * Lee una fila bloqueándola hasta el fin de la transacción ({@code SELECT ... FOR UPDATE}).
     * Si otra transacción la retiene más allá del tiempo de espera de la base, responde 409 para reintentar.
     */
    private <T> T bloquear(Class<T> tipo, Integer id, Supplier<String> mensajeNoEncontrado) {
        T entidad;
        try {
            entidad = entityManager.find(tipo, id, LockModeType.PESSIMISTIC_WRITE);
        } catch (PessimisticLockException | LockTimeoutException exception) {
            throw new ConflictoException("Otra programación simultánea usa los mismos recursos; reintentá la operación");
        }
        if (entidad == null) {
            throw new RecursoNoEncontradoException(mensajeNoEncontrado.get());
        }
        return entidad;
    }

    private static void exigirHabilitado(boolean activo, String mensaje) {
        if (!activo) {
            throw new ConflictoException(mensaje);
        }
    }

    private static boolean hayChoque(List<Partido> candidatos, LocalDateTime inicio, int excluirId) {
        return candidatos.stream()
                .filter(otro -> otro.getId() != excluirId)
                .anyMatch(otro -> HorarioPartido.chocan(inicio, otro.getFechaHoraInicio()));
    }
}