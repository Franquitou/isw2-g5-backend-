package isw2_G5.liga_amateur.users.repository;

import static org.assertj.core.api.Assertions.assertThat;

import isw2_G5.liga_amateur.security.PasswordConfiguration;
import isw2_G5.liga_amateur.users.dto.UsuarioCreateRequest;
import isw2_G5.liga_amateur.users.entity.Rol;
import isw2_G5.liga_amateur.users.service.UsuarioService;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Verifica usuario y RF-02 contra el SQL oficial solo en H2 embebida en memoria.
 * El datasource de @DataJpaTest reemplaza al archivo configurado para la aplicación.
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@Import({UsuarioService.class, PasswordConfiguration.class})
class UsuarioRepositoryTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UsuarioRepository users;

    @Autowired
    private UsuarioService service;

    @Test
    void testsUseOnlyMemoryDatabase() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL()).startsWith("jdbc:h2:mem:");
        }
    }

    @Test
    void readsSeededAdminAndFiltersInactiveUsers() {
        assertThat(users.findByUsername("admin")).get().satisfies(admin -> {
            assertThat(admin.getRol()).isEqualTo(Rol.ADMIN);
            assertThat(admin.getPasswordHash()).startsWith("$2");
            assertThat(admin.getFechaAlta()).isNotNull();
        });
        assertThat(users.findByActivo(false)).isEmpty();
    }

    @Test
    void createUsesSqlDefaultsAndCanBeQueriedWithoutPlaintextPassword() {
        var dto = service.create(new UsuarioCreateRequest(
                "delegado2", "clave-segura", "Delegado Dos", "DELEGADO"));

        assertThat(dto.id()).isPositive();
        assertThat(dto.fechaAlta()).isNotNull();
        assertThat(dto.rol()).isEqualTo("DELEGADO");
        assertThat(users.findByUsername("delegado2")).get().satisfies(saved -> {
            assertThat(saved.getPasswordHash()).startsWith("$2").isNotEqualTo("clave-segura");
            assertThat(saved.getUsername()).isEqualTo("delegado2");
        });
    }
}
