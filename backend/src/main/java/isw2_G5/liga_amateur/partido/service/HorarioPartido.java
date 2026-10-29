package isw2_G5.liga_amateur.partido.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Reglas de horario de los partidos: zona horaria de la liga y choque de horarios (RN-01 y RN-02).
 *
 * <p>La columna {@code partido.fecha_hora_inicio} es un {@code TIMESTAMP} sin zona. Según {@code docs/API.md} §1
 * se interpreta en {@code America/Argentina/Buenos_Aires}: al recibir un valor con offset se convierte a esa zona
 * antes de guardarlo o compararlo, y al responder se le vuelve a asociar la zona.</p>
 *
 * <p>Cada partido ocupa su cancha y su árbitro durante dos horas desde su inicio. Dos partidos chocan si sus
 * inicios están separados por <b>menos</b> de dos horas; una separación de exactamente dos horas es válida.
 * Qué partidos participan de la comparación (por ejemplo, excluir los suspendidos) lo decide quien consulta.</p>
 */
public final class HorarioPartido {

    /** Zona en la que se interpretan las fechas y horas de los partidos. */
    public static final ZoneId ZONA_LIGA = ZoneId.of("America/Argentina/Buenos_Aires");

    /** Tiempo durante el cual un partido ocupa su cancha y su árbitro. */
    public static final Duration DURACION_OCUPACION = Duration.ofHours(2);

    private HorarioPartido() {
    }

    /**
     * Convierte un instante con offset a la fecha y hora local de la liga, que es lo que se guarda en la base.
     *
     * @param inicio fecha y hora recibida en la API, con cualquier offset
     * @return fecha y hora equivalente en {@link #ZONA_LIGA}, sin zona
     */
    public static LocalDateTime aHoraLiga(OffsetDateTime inicio) {
        Objects.requireNonNull(inicio, "La fecha y hora de inicio es obligatoria");
        return inicio.atZoneSameInstant(ZONA_LIGA).toLocalDateTime();
    }

    /**
     * Asocia la zona de la liga a una fecha y hora leída de la base, para responderla en ISO-8601 con offset.
     *
     * @param inicioLiga fecha y hora local guardada en la base
     * @return el mismo momento con el offset de {@link #ZONA_LIGA}
     */
    public static OffsetDateTime conZonaLiga(LocalDateTime inicioLiga) {
        Objects.requireNonNull(inicioLiga, "La fecha y hora de inicio es obligatoria");
        return inicioLiga.atZone(ZONA_LIGA).toOffsetDateTime();
    }

    /**
     * Indica si dos partidos que comparten cancha o árbitro chocan (RN-01 y RN-02).
     * Ambos valores deben estar expresados en la zona de la liga.
     *
     * @param inicio inicio de un partido
     * @param otroInicio inicio del otro partido
     * @return {@code true} si los inicios están separados por menos de {@link #DURACION_OCUPACION}
     */
    public static boolean chocan(LocalDateTime inicio, LocalDateTime otroInicio) {
        Objects.requireNonNull(inicio, "La fecha y hora de inicio es obligatoria");
        Objects.requireNonNull(otroInicio, "La fecha y hora de inicio es obligatoria");
        return Duration.between(inicio, otroInicio).abs().compareTo(DURACION_OCUPACION) < 0;
    }
}