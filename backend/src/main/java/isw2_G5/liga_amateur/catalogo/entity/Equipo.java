package isw2_G5.liga_amateur.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Equipo de un torneo. Se mapea contra la tabla {@code equipo}.
 * La baja es lógica ({@code activo = false}) y no borra el historial.
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

    protected Equipo() {
        // Requerido por JPA.
    }

    /**
     * Crea un equipo activo dentro de un torneo existente.
     *
     * @param torneoId torneo al que pertenece el equipo
     * @param nombre nombre único dentro del torneo
     * @param barrio barrio del equipo, opcional
     */
    public Equipo(Integer torneoId, String nombre, String barrio) {
        this.torneoId = torneoId;
        this.nombre = nombre;
        this.barrio = barrio;
        this.activo = true;
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
