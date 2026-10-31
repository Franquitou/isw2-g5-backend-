package isw2_G5.liga_amateur.partido.repository;

import isw2_G5.liga_amateur.partido.entity.EstadoPartido;
import isw2_G5.liga_amateur.partido.entity.Partido;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;

/** Acceso a los partidos programados. */
public interface PartidoRepository extends JpaRepository<Partido, Integer> {

    /**
     * Candidatos a chocar con un horario en una cancha (RN-01). El rango es inclusivo y amplio a propósito:
     * la regla exacta de las dos horas la aplica {@code HorarioPartido.chocan}.
     *
     * @param canchaId cancha consultada
     * @param excluido estado que no participa de la comparación (suspendido)
     * @param desde inicio mínimo, inclusive
     * @param hasta inicio máximo, inclusive
     * @return partidos de la cancha en ese rango que no están en el estado excluido
     */
    List<Partido> findByCanchaIdAndEstadoNotAndFechaHoraInicioBetween(
            Integer canchaId, EstadoPartido excluido, LocalDateTime desde, LocalDateTime hasta);

    /**
     * Candidatos a chocar con un horario para un árbitro (RN-02). Mismo criterio que la consulta de cancha.
     *
     * @param arbitroId árbitro consultado
     * @param excluido estado que no participa de la comparación (suspendido)
     * @param desde inicio mínimo, inclusive
     * @param hasta inicio máximo, inclusive
     * @return partidos del árbitro en ese rango que no están en el estado excluido
     */
    List<Partido> findByArbitroIdAndEstadoNotAndFechaHoraInicioBetween(
            Integer arbitroId, EstadoPartido excluido, LocalDateTime desde, LocalDateTime hasta);
    /**
     * Indica si una jornada de un torneo tiene algún partido en alguno de los estados indicados.
     * Se usa para RN-07: no se inicia la jornada N mientras la N−1 tenga partidos sin terminar.
     *
     * @param torneoId torneo consultado
     * @param numeroFecha jornada consultada
     * @param estados estados buscados
     * @return {@code true} si existe al menos un partido así
     */
    boolean existsByTorneoIdAndNumeroFechaAndEstadoIn(
            Integer torneoId, Integer numeroFecha, Collection<EstadoPartido> estados);
    /**
     * Cuenta los partidos de una jornada en los que juega alguno de los equipos indicados.
     * Sirve para impedir que un equipo juegue dos veces en la misma jornada.
     *
     * @param torneoId torneo de la jornada
     * @param numeroFecha jornada consultada
     * @param equipos equipos a buscar, como local o visitante
     * @param excluido estado que no cuenta (suspendido)
     * @param excluirId partido que se ignora (el que se está editando), o 0 al crear
     * @return cantidad de partidos encontrados
     */
    @Query("""
            select count(p) from Partido p
            where p.torneoId = :torneoId
              and p.numeroFecha = :numeroFecha
              and p.estado <> :excluido
              and p.id <> :excluirId
              and (p.equipoLocalId in :equipos or p.equipoVisitanteId in :equipos)
            """)
    long contarPartidosDeEquiposEnFecha(@Param("torneoId") Integer torneoId,
                                        @Param("numeroFecha") Integer numeroFecha,
                                        @Param("equipos") List<Integer> equipos,
                                        @Param("excluido") EstadoPartido excluido,
                                        @Param("excluirId") Integer excluirId);
}