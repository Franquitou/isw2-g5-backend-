package isw2_G5.liga_amateur.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Representa usuario del esquema SQL, sin exponer la entidad ni su hash en respuestas REST.
 * La base completa fechaAlta: luego de un alta debe recargarse antes de responder con ese dato.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", nullable = false, unique = true, length = 40, updatable = false)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_alta", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaAlta;

    protected Usuario() {
        // Constructor requerido por JPA.
    }

    /** Crea un usuario con contraseña ya codificada; nunca recibe una contraseña en claro. */
    public Usuario(String username, String passwordHash, String nombreCompleto, Rol rol) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    /** Cambia solamente los datos editables de RF-02; username y fechaAlta no se modifican. */
    public void actualizar(String nombreCompleto, Rol rol) {
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
    }

    public void cambiarPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void cambiarActivo(boolean activo) {
        this.activo = activo;
    }
}
