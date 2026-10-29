package isw2_G5.liga_amateur.catalogo.repository;

import isw2_G5.liga_amateur.catalogo.entity.Equipo;
import java.util.List;
import java.util.Optional;
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
     * Busca un equipo por id sin importar su estado, para detectar duplicados.
     *
     * @param torneoId torneo consultado
     * @param nombre nombre del equipo
     * @return equipo encontrado, si existe
     */
    Optional<Equipo> findByTorneoIdAndNombre(Integer torneoId, String nombre);
}
