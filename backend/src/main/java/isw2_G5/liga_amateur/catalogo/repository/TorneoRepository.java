package isw2_G5.liga_amateur.catalogo.repository;

import isw2_G5.liga_amateur.catalogo.entity.Torneo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a los torneos precargados. */
public interface TorneoRepository extends JpaRepository<Torneo, Integer> {

    /**
     * Lista los torneos activos, del más reciente al más antiguo.
     *
     * @return torneos activos ordenados por año descendente y nombre
     */
    List<Torneo> findByActivoTrueOrderByAnioDescNombreAsc();
}
