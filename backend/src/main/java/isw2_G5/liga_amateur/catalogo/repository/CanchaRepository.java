package isw2_G5.liga_amateur.catalogo.repository;

import isw2_G5.liga_amateur.catalogo.entity.Cancha;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a las canchas precargadas. */
public interface CanchaRepository extends JpaRepository<Cancha, Integer> {

    /**
     * Filtra en la base, no en memoria, para traer solo las canchas habilitadas.
     *
     * @return canchas activas ordenadas por nombre
     */
    List<Cancha> findByActivoTrueOrderByNombreAsc();
}