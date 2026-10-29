package isw2_G5.liga_amateur.catalogo.repository;

import isw2_G5.liga_amateur.catalogo.entity.Jugador;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a los jugadores de cada equipo. */
public interface JugadorRepository extends JpaRepository<Jugador, Integer> {

    /**
     * Lista los jugadores activos de un equipo.
     *
     * @param equipoId equipo consultado
     * @return jugadores activos ordenados por apellido y nombre
     */
    List<Jugador> findByEquipoIdAndActivoTrueOrderByApellidoAscNombreAsc(Integer equipoId);

    /**
     * Busca un jugador por DNI sin importar su estado, para detectar duplicados.
     *
     * @param dni documento consultado
     * @return jugador encontrado, si existe
     */
    Optional<Jugador> findByDni(String dni);
}
