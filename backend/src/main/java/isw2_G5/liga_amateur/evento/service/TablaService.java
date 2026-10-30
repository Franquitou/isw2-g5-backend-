package isw2_G5.liga_amateur.evento.service;

import isw2_G5.liga_amateur.catalogo.entity.Equipo;
import isw2_G5.liga_amateur.catalogo.repository.EquipoRepository;
import isw2_G5.liga_amateur.catalogo.repository.TorneoRepository;
import isw2_G5.liga_amateur.common.error.RecursoNoEncontradoException;
import isw2_G5.liga_amateur.common.error.SolicitudInvalidaException;
import isw2_G5.liga_amateur.evento.dto.Marcador;
import isw2_G5.liga_amateur.evento.dto.PosicionResponse;
import isw2_G5.liga_amateur.evento.repository.PartidoLecturaRepository;
import isw2_G5.liga_amateur.partido.entity.EstadoPartido;
import isw2_G5.liga_amateur.partido.entity.Partido;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Tabla de posiciones pública de un torneo (RF-11). Solo cuentan los partidos FINALIZADO. */
@Service
@Transactional(readOnly = true)
public class TablaService {

    private final TorneoRepository torneoRepository;
    private final EquipoRepository equipoRepository;
    private final PartidoLecturaRepository partidoRepository;
    private final MarcadorService marcadorService;

    public TablaService(TorneoRepository torneoRepository, EquipoRepository equipoRepository,
            PartidoLecturaRepository partidoRepository, MarcadorService marcadorService) {
        this.torneoRepository = torneoRepository;
        this.equipoRepository = equipoRepository;
        this.partidoRepository = partidoRepository;
        this.marcadorService = marcadorService;
    }

    /**
     * Calcula la tabla de posiciones.
     *
     * <p>Incluye los equipos activos aunque no hayan jugado, y también equipos dados de baja que tengan
     * partidos finalizados, para no alterar el historial (RN-09).</p>
     *
     * @param torneoId torneo consultado
     * @return filas ordenadas por puntos, diferencia de gol, goles a favor y nombre
     * @throws SolicitudInvalidaException si el id no es positivo
     * @throws RecursoNoEncontradoException si el torneo no existe
     */
    public List<PosicionResponse> tabla(Integer torneoId) {
        if (torneoId == null || torneoId <= 0) {
            throw new SolicitudInvalidaException("El identificador de torneo debe ser un entero positivo");
        }
        if (!torneoRepository.existsById(torneoId)) {
            throw new RecursoNoEncontradoException("No existe el torneo con id " + torneoId);
        }
        List<Partido> finalizados = partidoRepository.findByTorneoIdAndEstado(torneoId, EstadoPartido.FINALIZADO);
        Map<Integer, Marcador> marcadores = marcadorService.calcularPorPartido(finalizados);

        Map<Integer, String> equipos = new HashMap<>();
        for (Equipo equipo : equipoRepository.findByTorneoIdAndActivoTrueOrderByNombreAsc(torneoId)) {
            equipos.put(equipo.getId(), equipo.getNombre());
        }
        Set<Integer> faltantes = new HashSet<>();
        for (Partido partido : finalizados) {
            if (!equipos.containsKey(partido.getEquipoLocalId())) {
                faltantes.add(partido.getEquipoLocalId());
            }
            if (!equipos.containsKey(partido.getEquipoVisitanteId())) {
                faltantes.add(partido.getEquipoVisitanteId());
            }
        }
        equipoRepository.findAllById(faltantes).forEach(e -> equipos.put(e.getId(), e.getNombre()));

        List<PosicionCalculator.ResultadoFinalizado> resultados = finalizados.stream()
                .map(p -> new PosicionCalculator.ResultadoFinalizado(p.getEquipoLocalId(),
                        p.getEquipoVisitanteId(), marcadores.get(p.getId()).golesLocal(),
                        marcadores.get(p.getId()).golesVisitante()))
                .toList();
        return PosicionCalculator.calcular(equipos, resultados);
    }
}
