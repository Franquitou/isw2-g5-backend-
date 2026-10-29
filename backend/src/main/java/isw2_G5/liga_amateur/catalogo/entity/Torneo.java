package isw2_G5.liga_amateur.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Torneo precargado de la liga. Se mapea contra la tabla {@code torneo} de
 * {@code schema.sql}; la API solo permite consultarlo (RF-05).
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

    /** Constructor requerido por JPA para reconstruir la entidad desde la base. */
    protected Torneo() {
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
