package isw2_G5.liga_amateur.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Jugador perteneciente a un equipo. Se mapea contra la tabla {@code jugador}.
 * El equipo es inmutable y la baja es lógica ({@code activo = false}).
 */
@Entity
@Table(name = "jugador")
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "equipo_id", nullable = false)
    private Integer equipoId;

    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 60)
    private String apellido;

    @Column(name = "dni", nullable = false, length = 10, unique = true)
    private String dni;

    @Column(name = "numero_camiseta")
    private Integer numeroCamiseta;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    protected Jugador() {
        // Requerido por JPA.
    }

    /**
     * Crea un jugador activo dentro de un equipo existente.
     *
     * @param equipoId equipo al que pertenece el jugador
     * @param nombre nombre del jugador
     * @param apellido apellido del jugador
     * @param dni documento único en toda la liga
     * @param numeroCamiseta número de camiseta entre 1 y 99, o {@code null}
     */
    public Jugador(Integer equipoId, String nombre, String apellido, String dni, Integer numeroCamiseta) {
        this.equipoId = equipoId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.numeroCamiseta = numeroCamiseta;
        this.activo = true;
    }

    public Integer getId() {
        return id;
    }

    public Integer getEquipoId() {
        return equipoId;
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

    public Integer getNumeroCamiseta() {
        return numeroCamiseta;
    }

    public boolean isActivo() {
        return activo;
    }
}
