package isw2_G5.liga_amateur.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Torneo precargado de la liga. Se mapea contra la tabla {@code torneo} de
 * {@code schema.sql}; no se crean ni modifican torneos desde la API.
 */
@Entity
@Table(name = "torneo")
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    protected Torneo() {
        // Requerido por JPA.
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getAnio() {
        return anio;
    }

    public boolean isActivo() {
        return activo;
    }
}
