package isw2_G5.liga_amateur.evento.service;

import isw2_G5.liga_amateur.catalogo.entity.Jugador;
import isw2_G5.liga_amateur.catalogo.repository.JugadorRepository;
import isw2_G5.liga_amateur.common.error.ConflictoException;
import isw2_G5.liga_amateur.common.error.RecursoNoEncontradoException;
import isw2_G5.liga_amateur.common.error.SolicitudInvalidaException;
import isw2_G5.liga_amateur.evento.dto.EventoCreateRequest;
import isw2_G5.liga_amateur.evento.dto.EventoResponse;
import isw2_G5.liga_amateur.evento.entity.EventoPartido;
import isw2_G5.liga_amateur.evento.repository.EventoPartidoRepository;
import isw2_G5.liga_amateur.evento.repository.PartidoLecturaRepository;
import isw2_G5.liga_amateur.partido.entity.EstadoPartido;
import isw2_G5.liga_amateur.partido.entity.Partido;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro y corrección de eventos de un partido en juego (RF-08 y RF-09). */
@Service
@Transactional(readOnly = true)
public class EventoService {

    private final EventoPartidoRepository eventoRepository;
    private final PartidoLecturaRepository partidoRepository;
    private final JugadorRepository jugadorRepository;
    private final SuspensionService suspensionService;

    public EventoService(EventoPartidoRepository eventoRepository, PartidoLecturaRepository partidoRepository,
            JugadorRepository jugadorRepository, SuspensionService suspensionService) {
        this.eventoRepository = eventoRepository;
        this.partidoRepository = partidoRepository;
        this.jugadorRepository = jugadorRepository;
        this.suspensionService = suspensionService;
    }

    /**
     * Registra un evento (RF-08, RN-03, RN-04).
     *
     * @param partidoId partido de la ruta
     * @param request datos validados del evento
     * @return el evento creado
     * @throws SolicitudInvalidaException si algún id no es positivo
     * @throws RecursoNoEncontradoException si el partido o el jugador no existen
     * @throws ConflictoException si el partido no está EN_JUEGO, el jugador está inactivo, no pertenece a
     *         ninguno de los dos equipos o está suspendido en la jornada del partido
     */
    @Transactional
    public EventoResponse crear(Integer partidoId, EventoCreateRequest request) {
        requerirPositivo(partidoId, "partido");
        requerirPositivo(request.jugadorId(), "jugador");
        Partido partido = buscarPartido(partidoId);
        Jugador jugador = jugadorRepository.findById(request.jugadorId()).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe el jugador con id " + request.jugadorId()));
        exigirEnJuego(partido, "registrar");
        if (!jugador.isActivo()) {
            throw new ConflictoException("El jugador está inactivo y no puede registrar eventos");
        }
        if (!Objects.equals(jugador.getEquipoId(), partido.getEquipoLocalId())
                && !Objects.equals(jugador.getEquipoId(), partido.getEquipoVisitanteId())) {
            throw new ConflictoException("El jugador no pertenece a ninguno de los equipos del partido");
        }
        suspensionService.motivoDeSuspension(partido.getTorneoId(), partido.getNumeroFecha(), jugador.getId())
                .ifPresent(motivo -> {
                    throw new ConflictoException("El jugador está suspendido en esta fecha (" + motivo + ")");
                });
        EventoPartido guardado = eventoRepository.save(
                new EventoPartido(partidoId, jugador.getId(), request.tipo(), request.minuto()));
        return EventoResponse.from(guardado);
    }

    /**
     * Elimina un evento de un partido en juego (RF-09). El marcador se recalcula solo, porque se deriva de los
     * eventos.
     *
     * @param partidoId partido de la ruta
     * @param eventoId evento a eliminar
     * @throws SolicitudInvalidaException si algún id no es positivo
     * @throws RecursoNoEncontradoException si el partido no existe o el evento no existe o es de otro partido
     * @throws ConflictoException si el partido no está EN_JUEGO
     */
    @Transactional
    public void eliminar(Integer partidoId, Integer eventoId) {
        requerirPositivo(partidoId, "partido");
        requerirPositivo(eventoId, "evento");
        Partido partido = buscarPartido(partidoId);
        EventoPartido evento = eventoRepository.findById(eventoId)
                .filter(e -> Objects.equals(e.getPartidoId(), partidoId))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el evento con id " + eventoId + " en el partido " + partidoId));
        exigirEnJuego(partido, "corregir");
        eventoRepository.delete(evento);
    }

    private Partido buscarPartido(Integer partidoId) {
        return partidoRepository.findById(partidoId).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe el partido con id " + partidoId));
    }

    private static void exigirEnJuego(Partido partido, String accion) {
        if (partido.getEstado() != EstadoPartido.EN_JUEGO) {
            throw new ConflictoException("Solo se pueden " + accion + " eventos en un partido EN_JUEGO");
        }
    }

    private static void requerirPositivo(Integer valor, String nombre) {
        if (valor == null || valor <= 0) {
            throw new SolicitudInvalidaException("El identificador de " + nombre + " debe ser un entero positivo");
        }
    }
}
