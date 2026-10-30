package isw2_G5.liga_amateur.evento.service;

import isw2_G5.liga_amateur.catalogo.entity.Jugador;
import isw2_G5.liga_amateur.catalogo.repository.JugadorRepository;
import isw2_G5.liga_amateur.evento.dto.Marcador;
import isw2_G5.liga_amateur.evento.entity.EventoPartido;
import isw2_G5.liga_amateur.evento.entity.TipoEvento;
import isw2_G5.liga_amateur.evento.repository.EventoPartidoRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Único responsable del cálculo del marcador (RN-06). El listado de partidos, el detalle y la tabla
 * de posiciones deben usar este servicio para no duplicar la regla.
 */
@Service
@Transactional(readOnly = true)
public class MarcadorService {

    private final EventoPartidoRepository eventoRepository;
    private final JugadorRepository jugadorRepository;

    public MarcadorService(EventoPartidoRepository eventoRepository, JugadorRepository jugadorRepository) {
        this.eventoRepository = eventoRepository;
        this.jugadorRepository = jugadorRepository;
    }

    /**
     * Calcula el marcador de un partido a partir de sus goles. Cada gol suma al equipo
     * del jugador que lo hizo; sin goles el resultado es 0-0.
     *
     * @param partidoId partido consultado
     * @param equipoLocalId equipo local del partido
     * @param equipoVisitanteId equipo visitante del partido
     * @return marcador del partido
     */
    public Marcador calcular(Integer partidoId, Integer equipoLocalId, Integer equipoVisitanteId) {
        List<EventoPartido> goles = eventoRepository.findByPartidoIdAndTipo(partidoId, TipoEvento.GOL);
        if (goles.isEmpty()) {
            return new Marcador(0, 0);
        }
        List<Integer> jugadorIds = goles.stream().map(EventoPartido::getJugadorId).distinct().toList();
        Map<Integer, Integer> equipoPorJugador = jugadorRepository.findAllById(jugadorIds).stream()
                .collect(Collectors.toMap(Jugador::getId, Jugador::getEquipoId));
        List<Integer> equiposDeGoleadores = goles.stream()
                .map(gol -> equipoPorJugador.get(gol.getJugadorId()))
                .toList();
        return MarcadorCalculator.calcular(equipoLocalId, equipoVisitanteId, equiposDeGoleadores);
    }
}
