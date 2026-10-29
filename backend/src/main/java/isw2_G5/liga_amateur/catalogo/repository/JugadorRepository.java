package isw2_G5.liga_amateur.catalogo.repository;

import isw2_G5.liga_amateur.catalogo.entity.Jugador;
import java.util.List;
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
     * Indica si el DNI ya está registrado, sin importar el estado del jugador.
     *
     * @param dni documento consultado
     * @return {@code true} si el DNI ya existe
     */
    boolean existsByDni(String dni);

    /**
     * Indica si otro jugador ya usa ese DNI.
     *
     * @param dni documento consultado
     * @param id identificador que se excluye de la búsqueda
     * @return {@code true} si otro jugador usa el DNI
     */
    boolean existsByDniAndIdNot(String dni, Integer id);
}
