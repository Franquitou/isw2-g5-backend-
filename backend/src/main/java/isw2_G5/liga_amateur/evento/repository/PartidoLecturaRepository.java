package isw2_G5.liga_amateur.evento.repository;

import isw2_G5.liga_amateur.partido.entity.EstadoPartido;
import isw2_G5.liga_amateur.partido.entity.Partido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Lectura de partidos para eventos, tabla y suspendidos. No reemplaza al repositorio de programación de José. */
public interface PartidoLecturaRepository extends JpaRepository<Partido, Integer> {

    /**
     * Lista los partidos de un torneo en un estado dado.
     *
     * @param torneoId torneo consultado
     * @param estado estado buscado
     * @return partidos que cumplen ambos criterios
     */
    List<Partido> findByTorneoIdAndEstado(Integer torneoId, EstadoPartido estado);
}
