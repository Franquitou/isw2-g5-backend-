package isw2_G5.liga_amateur.partido.entity;

import java.util.Objects;

/**
 * Estados posibles de un partido y sus transiciones válidas (RN-05).
 * Los nombres coinciden exactamente con el {@code CHECK} de la columna {@code partido.estado} en {@code schema.sql},
 * por lo que la entidad deberá persistirlo con {@code @Enumerated(EnumType.STRING)}.
 *
 * <p>Transiciones permitidas: {@code PROGRAMADO → EN_JUEGO | SUSPENDIDO} y
 * {@code EN_JUEGO → FINALIZADO | SUSPENDIDO}. {@code FINALIZADO} y {@code SUSPENDIDO} son estados finales.
 * Un partido suspendido no se reprograma: se crea otro partido (RF-06).</p>
 */
public enum EstadoPartido {

    /** Estado inicial de todo partido creado. */
    PROGRAMADO,

    /** El partido se está disputando; solo en este estado se registran eventos. */
    EN_JUEGO,

    /** El partido terminó; es el único estado que suma a la tabla y a las suspensiones. */
    FINALIZADO,

    /** El partido no se completó; no cuenta para el choque de horarios ni para la tabla. */
    SUSPENDIDO;

    /**
     * Indica si un partido en este estado puede pasar al estado indicado.
     * Concentra la regla RN-05 en un solo lugar para que ningún servicio la repita.
     *
     * @param destino estado al que se quiere pasar; no puede ser {@code null}
     * @return {@code true} si la transición está permitida
     */
    public boolean puedeCambiarA(EstadoPartido destino) {
        Objects.requireNonNull(destino, "El estado de destino es obligatorio");
        return switch (this) {
            case PROGRAMADO -> destino == EN_JUEGO || destino == SUSPENDIDO;
            case EN_JUEGO -> destino == FINALIZADO || destino == SUSPENDIDO;
            case FINALIZADO, SUSPENDIDO -> false;
        };
    }

    /**
     * Indica si el estado es final, es decir, si ya no admite ninguna transición.
     *
     * @return {@code true} para {@code FINALIZADO} y {@code SUSPENDIDO}
     */
    public boolean esFinal() {
        return this == FINALIZADO || this == SUSPENDIDO;
    }
}