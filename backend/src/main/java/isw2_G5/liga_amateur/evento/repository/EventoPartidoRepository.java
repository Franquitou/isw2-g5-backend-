package isw2_G5.liga_amateur.evento.repository;

import isw2_G5.liga_amateur.evento.entity.EventoPartido;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Acceso a los eventos de los partidos y a las consultas derivadas de ellos. */
public interface EventoPartidoRepository extends JpaRepository<EventoPartido, Integer> {

    /**
     * Lista los eventos de un partido.
     *
     * @param partidoId partido consultado
     * @return eventos ordenados por minuto y luego por id
     */
    List<EventoPartido> findByPartidoIdOrderByMinutoAscIdAsc(Integer partidoId);

    /**
     * Devuelve los goles de varios partidos con el equipo del goleador.
     *
     * @param partidoIds partidos consultados (no vacío)
     * @return un elemento por gol
     */
    @Query("""
            select new isw2_G5.liga_amateur.evento.repository.GolDeEquipo(e.partidoId, j.equipoId)
            from EventoPartido e, Jugador j
            where e.jugadorId = j.id
              and e.tipo = isw2_G5.liga_amateur.evento.entity.TipoEvento.GOL
              and e.partidoId in :partidoIds
            """)
    List<GolDeEquipo> golesDeEquipo(@Param("partidoIds") Collection<Integer> partidoIds);

    /**
     * Tarjetas de los partidos FINALIZADO de una jornada de un torneo.
     *
     * @param torneoId torneo
     * @param numeroFecha jornada de las tarjetas
     * @return amarillas y rojas de esa jornada
     */
    @Query("""
            select new isw2_G5.liga_amateur.evento.repository.TarjetaDeJugador(e.partidoId, e.jugadorId, j.equipoId, e.tipo)
            from EventoPartido e, Jugador j, Partido p
            where e.jugadorId = j.id
              and e.partidoId = p.id
              and p.torneoId = :torneoId
              and p.numeroFecha = :numeroFecha
              and p.estado = isw2_G5.liga_amateur.partido.entity.EstadoPartido.FINALIZADO
              and e.tipo in (isw2_G5.liga_amateur.evento.entity.TipoEvento.AMARILLA,
                             isw2_G5.liga_amateur.evento.entity.TipoEvento.ROJA)
            """)
    List<TarjetaDeJugador> tarjetasDeJornada(@Param("torneoId") Integer torneoId,
            @Param("numeroFecha") Integer numeroFecha);

    /**
     * Igual que {@link #tarjetasDeJornada} pero solo de un jugador.
     *
     * @param torneoId torneo
     * @param numeroFecha jornada de las tarjetas
     * @param jugadorId jugador consultado
     * @return amarillas y rojas del jugador en esa jornada
     */
    @Query("""
            select new isw2_G5.liga_amateur.evento.repository.TarjetaDeJugador(e.partidoId, e.jugadorId, j.equipoId, e.tipo)
            from EventoPartido e, Jugador j, Partido p
            where e.jugadorId = j.id
              and e.partidoId = p.id
              and e.jugadorId = :jugadorId
              and p.torneoId = :torneoId
              and p.numeroFecha = :numeroFecha
              and p.estado = isw2_G5.liga_amateur.partido.entity.EstadoPartido.FINALIZADO
              and e.tipo in (isw2_G5.liga_amateur.evento.entity.TipoEvento.AMARILLA,
                             isw2_G5.liga_amateur.evento.entity.TipoEvento.ROJA)
            """)
    List<TarjetaDeJugador> tarjetasDeJugadorEnJornada(@Param("torneoId") Integer torneoId,
            @Param("numeroFecha") Integer numeroFecha, @Param("jugadorId") Integer jugadorId);
}
