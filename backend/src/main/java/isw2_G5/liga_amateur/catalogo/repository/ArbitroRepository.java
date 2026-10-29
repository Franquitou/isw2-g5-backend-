package isw2_G5.liga_amateur.catalogo.repository;

import isw2_G5.liga_amateur.catalogo.entity.Arbitro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a los árbitros precargados. */
public interface ArbitroRepository extends JpaRepository<Arbitro, Integer> {

    /**
     * Filtra en la base, no en memoria, para traer solo los árbitros habilitados.
     *
     * @return árbitros activos ordenados por apellido y nombre
     */
    List<Arbitro> findByActivoTrueOrderByApellidoAscNombreAsc();
}