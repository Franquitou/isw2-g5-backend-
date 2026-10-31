package isw2_G5.liga_amateur.evento.service;

import isw2_G5.liga_amateur.common.error.RecursoNoEncontradoException;
import isw2_G5.liga_amateur.evento.dto.Marcador;
import isw2_G5.liga_amateur.evento.repository.EventoPartidoRepository;
import isw2_G5.liga_amateur.evento.repository.GolDeEquipo;
import isw2_G5.liga_amateur.evento.repository.PartidoLecturaRepository;
import isw2_G5.liga_amateur.partido.entity.Partido;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Único punto de cálculo del marcador (RN-06). El listado de José, el detalle y la tabla usan este servicio;
 * ninguno duplica la regla.
 */
@Service
@Transactional(readOnly = true)
public class MarcadorService {

    private final EventoPartidoRepository eventoRepository;
    private final PartidoLecturaRepository partidoRepository;

    public MarcadorService(EventoPartidoRepository eventoRepository, PartidoLecturaRepository partidoRepository) {
        this.eventoRepository = eventoRepository;
        this.partidoRepository = partidoRepository;
    }

    /**
     * Calcula el marcador de un partido.
     *
     * @param partidoId identificador del partido
     * @return goles de cada equipo; 0-0 si no hay goles
     * @throws RecursoNoEncontradoException si el partido no existe
     */
    public Marcador calcular(Integer partidoId) {
        Partido partido = partidoRepository.findById(partidoId).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe el partido con id " + partidoId));
        return calcularPorPartido(List.of(partido)).get(partido.getId());
    }

    /**
     * Calcula los marcadores de varios partidos con una sola consulta de goles (útil para listados y tabla).
     *
     * @param partidos partidos ya cargados
     * @return marcador por id de partido; todos los partidos recibidos tienen entrada
     */
    public Map<Integer, Marcador> calcularPorPartido(Collection<Partido> partidos) {
        Map<Integer, Marcador> resultado = new HashMap<>();
        if (partidos.isEmpty()) {
            return resultado;
        }
        Map<Integer, List<Integer>> equiposPorPartido = new HashMap<>();
        List<Integer> ids = partidos.stream().map(Partido::getId).toList();
        for (GolDeEquipo gol : eventoRepository.golesDeEquipo(ids)) {
            equiposPorPartido.computeIfAbsent(gol.partidoId(), k -> new java.util.ArrayList<>())
                    .add(gol.equipoId());
        }
        for (Partido partido : partidos) {
            resultado.put(partido.getId(), MarcadorCalculator.calcular(partido.getEquipoLocalId(),
                    partido.getEquipoVisitanteId(), equiposPorPartido.getOrDefault(partido.getId(), List.of())));
        }
        return resultado;
    }
}
