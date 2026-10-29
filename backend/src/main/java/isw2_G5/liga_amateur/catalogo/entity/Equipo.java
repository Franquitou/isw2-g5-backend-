package isw2_G5.liga_amateur.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Equipo de un torneo. Se mapea contra la tabla {@code equipo} de {@code schema.sql}.
 * La baja es lógica ({@code activo = false}) y no borra el historial (RN-09).
 */
@Entity
@Table(name = "equipo")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "torneo_id", nullable = false)
    private Integer torneoId;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "barrio", length = 80)
    private String barrio;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    /** Constructor requerido por JPA para reconstruir la entidad desde la base. */
    protected Equipo() {
    }

    /**
     * Crea un equipo activo dentro de un torneo.
     *
     * @param torneoId torneo al que pertenece
     * @param nombre nombre único dentro del torneo
     * @param barrio barrio, puede ser {@code null}
     */
    public Equipo(Integer torneoId, String nombre, String barrio) {
        this.torneoId = torneoId;
        this.nombre = nombre;
        this.barrio = barrio;
        this.activo = true;
    }

    /**
     * Actualiza los datos editables sin cambiar de torneo.
     *
     * @param nombre nuevo nombre, único dentro del torneo
     * @param barrio nuevo barrio, puede ser {@code null}
     */
    public void actualizar(String nombre, String barrio) {
        this.nombre = nombre;
        this.barrio = barrio;
    }

    /** Aplica la baja lógica exigida por RN-09. */
    public void darDeBaja() {
        this.activo = false;
    }

    public Integer getId() {
        return id;
    }

    public Integer getTorneoId() {
        return torneoId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getBarrio() {
        return barrio;
    }

    public boolean isActivo() {
        return activo;
    }
}
