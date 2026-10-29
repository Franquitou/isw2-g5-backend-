package isw2_G5.liga_amateur.evento.repository;

import isw2_G5.liga_amateur.evento.entity.EventoPartido;
import isw2_G5.liga_amateur.evento.entity.TipoEvento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a los eventos registrados en cada partido. */
public interface EventoPartidoRepository extends JpaRepository<EventoPartido, Integer> {

    /**
     * Lista los eventos de un partido en el orden en que ocurrieron.
     *
     * @param partidoId partido consultado
     * @return eventos ordenados por minuto y, a igual minuto, por orden de carga
     */
    List<EventoPartido> findByPartidoIdOrderByMinutoAscIdAsc(Integer partidoId);

    /**
     * Lista los eventos de un partido de un tipo dado, por ejemplo los goles para calcular el marcador.
     *
     * @param partidoId partido consultado
     * @param tipo tipo de evento buscado
     * @return eventos del partido de ese tipo
     */
    List<EventoPartido> findByPartidoIdAndTipo(Integer partidoId, TipoEvento tipo);

    /**
     * Busca un evento comprobando que pertenece al partido indicado, para no borrar el de otro partido.
     *
     * @param id identificador del evento
     * @param partidoId partido al que debe pertenecer
     * @return el evento, o vacío si no existe o es de otro partido
     */
    Optional<EventoPartido> findByIdAndPartidoId(Integer id, Integer partidoId);
}
