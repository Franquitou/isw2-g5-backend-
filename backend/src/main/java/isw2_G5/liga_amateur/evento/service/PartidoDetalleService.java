package isw2_G5.liga_amateur.evento.service;

import isw2_G5.liga_amateur.common.error.RecursoNoEncontradoException;
import isw2_G5.liga_amateur.common.error.SolicitudInvalidaException;
import isw2_G5.liga_amateur.evento.dto.EventoResponse;
import isw2_G5.liga_amateur.evento.dto.Marcador;
import isw2_G5.liga_amateur.evento.dto.PartidoDetalleResponse;
import isw2_G5.liga_amateur.evento.repository.EventoPartidoRepository;
import isw2_G5.liga_amateur.evento.repository.PartidoLecturaRepository;
import isw2_G5.liga_amateur.partido.entity.Partido;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Detalle público de un partido con resultado y eventos (RF-10). */
@Service
@Transactional(readOnly = true)
public class PartidoDetalleService {

    private final PartidoLecturaRepository partidoRepository;
    private final EventoPartidoRepository eventoRepository;
    private final MarcadorService marcadorService;

    public PartidoDetalleService(PartidoLecturaRepository partidoRepository,
            EventoPartidoRepository eventoRepository, MarcadorService marcadorService) {
        this.partidoRepository = partidoRepository;
        this.eventoRepository = eventoRepository;
        this.marcadorService = marcadorService;
    }

    /**
     * Devuelve el detalle de un partido.
     *
     * @param partidoId identificador del partido
     * @return datos del partido, resultado derivado y eventos por minuto
     * @throws SolicitudInvalidaException si el id no es positivo
     * @throws RecursoNoEncontradoException si el partido no existe
     */
    public PartidoDetalleResponse detalle(Integer partidoId) {
        if (partidoId == null || partidoId <= 0) {
            throw new SolicitudInvalidaException("El identificador de partido debe ser un entero positivo");
        }
        Partido partido = partidoRepository.findById(partidoId).orElseThrow(
                () -> new RecursoNoEncontradoException("No existe el partido con id " + partidoId));
        Marcador resultado = marcadorService.calcular(partidoId);
        List<EventoResponse> eventos = eventoRepository.findByPartidoIdOrderByMinutoAscIdAsc(partidoId).stream()
                .map(EventoResponse::from).toList();
        return PartidoDetalleResponse.from(partido, resultado, eventos);
    }
}
