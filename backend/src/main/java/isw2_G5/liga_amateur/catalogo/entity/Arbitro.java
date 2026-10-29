package isw2_G5.liga_amateur.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Árbitro que dirige los partidos. Se mapea sobre la tabla {@code arbitro} de {@code schema.sql}.
 * Son datos precargados de solo lectura: el sistema no ofrece altas ni modificaciones de árbitros,
 * por eso la entidad no expone setters.
 */
@Entity
@Table(name = "arbitro")
public class Arbitro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 60)
    private String apellido;

    @Column(name = "dni", nullable = false, unique = true, length = 10)
    private String dni;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    /** Constructor requerido por JPA para reconstruir la entidad desde la base. */
    protected Arbitro() {
    }

    /**
     * Crea un árbitro con sus datos; pensado para pruebas, ya que el sistema no da de alta árbitros.
     *
     * @param nombre nombre del árbitro
     * @param apellido apellido del árbitro
     * @param dni documento único, tratado como texto
     * @param activo si el árbitro está habilitado
     */
    public Arbitro(String nombre, String apellido, String dni, boolean activo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.activo = activo;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getDni() {
        return dni;
    }

    public boolean isActivo() {
        return activo;
    }
}