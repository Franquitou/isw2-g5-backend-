package isw2_G5.liga_amateur.users.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

/** Valida DTOs RF-02 sin Spring, repositorios ni base de datos. */
class UsuarioDtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void creationRequiresCompleteNameUsernameRoleAndEightCharacterPassword() {
        assertThat(validator.validate(new UsuarioCreateRequest("", "corta", "", "OTRO")))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("username", "password", "nombreCompleto", "rol");
    }

    @Test
    void creationAcceptsDocumentedRolesAndPasswordLength() {
        assertThat(validator.validate(new UsuarioCreateRequest("delegado1", "12345678", "Delegado Uno", "DELEGADO")))
                .isEmpty();
        assertThat(validator.validate(new UsuarioCreateRequest("admin2", "12345678", "Admin Dos", "ADMIN")))
                .isEmpty();
    }

    @Test
    void putRequiresBothEditableFields() {
        assertThat(validator.validate(new UsuarioUpdateRequest(null, null)))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("nombreCompleto", "rol");
    }

    @Test
    void passwordUpdateRejectsTooShortValue() {
        assertThat(validator.validate(new PasswordUpdateRequest("1234567"))).isNotEmpty();
        assertThat(validator.validate(new PasswordUpdateRequest("12345678"))).isEmpty();
    }

    @Test
    void estadoRequiresBooleanButAcceptsFalse() {
        assertThat(validator.validate(new UsuarioEstadoRequest(null))).isNotEmpty();
        assertThat(validator.validate(new UsuarioEstadoRequest(false))).isEmpty();
    }
}
