package isw2_G5.liga_amateur.catalogo.repository;

import isw2_G5.liga_amateur.catalogo.entity.Arbitro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a los árbitros precargados. Solo se consultan árbitros activos. */
public interface ArbitroRepository extends JpaRepository<Arbitro, Integer> {

    /**
     * Lista los árbitros activos ordenados por apellido y nombre.
     *
     * @return árbitros activos
     */
    List<Arbitro> findByActivoTrueOrderByApellidoAscNombreAsc();
}
