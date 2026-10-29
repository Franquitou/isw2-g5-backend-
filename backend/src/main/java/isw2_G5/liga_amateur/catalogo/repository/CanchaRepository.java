package isw2_G5.liga_amateur.catalogo.repository;

import isw2_G5.liga_amateur.catalogo.entity.Cancha;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a las canchas precargadas. Solo se consultan canchas activas. */
public interface CanchaRepository extends JpaRepository<Cancha, Integer> {

    /**
     * Lista las canchas activas ordenadas por nombre.
     *
     * @return canchas activas
     */
    List<Cancha> findByActivoTrueOrderByNombreAsc();
}
