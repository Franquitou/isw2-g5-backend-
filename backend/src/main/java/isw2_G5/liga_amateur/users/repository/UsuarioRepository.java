package isw2_G5.liga_amateur.users.repository;

import isw2_G5.liga_amateur.users.entity.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a usuarios en la tabla oficial sin generar ni modificar el esquema. */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    List<Usuario> findByActivo(boolean activo);
}
