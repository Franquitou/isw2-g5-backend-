package isw2_G5.liga_amateur.evento.service;

import isw2_G5.liga_amateur.catalogo.repository.TorneoRepository;
import isw2_G5.liga_amateur.common.error.RecursoNoEncontradoException;
import isw2_G5.liga_amateur.common.error.SolicitudInvalidaException;
import isw2_G5.liga_amateur.evento.dto.SuspendidoResponse;
import isw2_G5.liga_amateur.evento.entity.MotivoSuspension;
import isw2_G5.liga_amateur.evento.repository.EventoPartidoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Suspensiones calculadas dinámicamente desde los partidos FINALIZADO de la jornada N-1 (RF-12, P-02).
 * No hay tabla de suspensiones ni se traslada una sanción si no existe una fecha posterior.
 */
@Service
@Transactional(readOnly = true)
public class SuspensionService {

    private final EventoPartidoRepository eventoRepository;
    private final TorneoRepository torneoRepository;

    public SuspensionService(EventoPartidoRepository eventoRepository, TorneoRepository torneoRepository) {
        this.eventoRepository = eventoRepository;
        this.torneoRepository = torneoRepository;
    }

    /**
     * Lista los jugadores suspendidos en una jornada.
     *
     * @param torneoId torneo consultado
     * @param numeroFecha jornada consultada, desde 1
     * @return suspendidos de la jornada; vacía para la jornada 1
     * @throws SolicitudInvalidaException si algún identificador o la jornada no son positivos
     * @throws RecursoNoEncontradoException si el torneo no existe
     */
    public List<SuspendidoResponse> listar(Integer torneoId, Integer numeroFecha) {
        requerirPositivo(torneoId, "torneo");
        requerirPositivo(numeroFecha, "jornada");
        if (!torneoRepository.existsById(torneoId)) {
            throw new RecursoNoEncontradoException("No existe el torneo con id " + torneoId);
        }
        if (numeroFecha == 1) {
            return List.of();
        }
        int jornadaSancion = numeroFecha - 1;
        return SuspensionCalculator.calcular(eventoRepository.tarjetasDeJornada(torneoId, jornadaSancion)).stream()
                .map(s -> new SuspendidoResponse(s.jugadorId(), s.equipoId(), numeroFecha, s.motivo(),
                        jornadaSancion))
                .toList();
    }

    /**
     * Indica por qué un jugador está suspendido en una jornada, si lo está (RN-03/RN-04).
     *
     * @param torneoId torneo del partido
     * @param numeroFecha jornada del partido en el que se quiere registrar un evento
     * @param jugadorId jugador consultado
     * @return motivo de la suspensión, o vacío si puede jugar
     */
    public Optional<MotivoSuspension> motivoDeSuspension(Integer torneoId, Integer numeroFecha, Integer jugadorId) {
        if (numeroFecha <= 1) {
            return Optional.empty();
        }
        return SuspensionCalculator
                .calcular(eventoRepository.tarjetasDeJugadorEnJornada(torneoId, numeroFecha - 1, jugadorId))
                .stream().findFirst().map(SuspensionCalculator.Sancion::motivo);
    }

    private static void requerirPositivo(Integer valor, String nombre) {
        if (valor == null || valor <= 0) {
            throw new SolicitudInvalidaException("El identificador de " + nombre + " debe ser un entero positivo");
        }
    }
}
