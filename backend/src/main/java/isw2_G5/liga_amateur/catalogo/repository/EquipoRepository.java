package isw2_G5.liga_amateur.catalogo.repository;

import isw2_G5.liga_amateur.catalogo.entity.Equipo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a los equipos de cada torneo. */
public interface EquipoRepository extends JpaRepository<Equipo, Integer> {

    /**
     * Lista los equipos activos de un torneo.
     *
     * @param torneoId torneo consultado
     * @return equipos activos ordenados por nombre
     */
    List<Equipo> findByTorneoIdAndActivoTrueOrderByNombreAsc(Integer torneoId);

    /**
     * Indica si ya existe un equipo con ese nombre en el torneo, sin importar su estado.
     *
     * @param torneoId torneo consultado
     * @param nombre nombre del equipo
     * @return {@code true} si el nombre ya está usado en el torneo
     */
    boolean existsByTorneoIdAndNombre(Integer torneoId, String nombre);

    /**
     * Indica si otro equipo del torneo ya usa ese nombre.
     *
     * @param torneoId torneo consultado
     * @param nombre nombre del equipo
     * @param id identificador que se excluye de la búsqueda
     * @return {@code true} si otro equipo usa el nombre
     */
    boolean existsByTorneoIdAndNombreAndIdNot(Integer torneoId, String nombre, Integer id);
}
