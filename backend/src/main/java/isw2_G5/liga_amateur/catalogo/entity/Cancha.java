package isw2_G5.liga_amateur.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cancha donde se disputan los partidos. Se mapea sobre la tabla {@code cancha} de {@code schema.sql}.
 * Son datos precargados de solo lectura: el sistema no ofrece altas ni modificaciones de canchas,
 * por eso la entidad no expone setters.
 */
@Entity
@Table(name = "cancha")
public class Cancha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(name = "direccion", length = 120)
    private String direccion;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    /** Constructor requerido por JPA para reconstruir la entidad desde la base. */
    protected Cancha() {
    }

    /**
     * Crea una cancha con sus datos; pensado para pruebas, ya que el sistema no da de alta canchas.
     *
     * @param nombre nombre único de la cancha
     * @param direccion dirección, puede ser {@code null}
     * @param activo si la cancha está habilitada
     */
    public Cancha(String nombre, String direccion, boolean activo) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.activo = activo;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public boolean isActivo() {
        return activo;
    }
}