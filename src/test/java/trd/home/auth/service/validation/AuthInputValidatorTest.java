package trd.home.auth.service.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.Test;
import trd.home.auth.constant.UserRole;
import trd.home.auth.exception.InvalidCredentialException;

class AuthInputValidatorTest {

    private final AuthInputValidator validator = new AuthInputValidator();

    @Test
    void acceptsValidInput() {
        assertDoesNotThrow(() -> validator.credentials("alice", "password"));
        assertDoesNotThrow(() -> validator.userId("user-id"));
        assertDoesNotThrow(() -> validator.roles(Set.of(UserRole.ADMIN)));
    }

    @Test
    void rejectsMissingInput() {
        assertThrows(InvalidCredentialException.class, () -> validator.username(" "));
        assertThrows(InvalidCredentialException.class, () -> validator.password(null));
        assertThrows(InvalidCredentialException.class, () -> validator.userId(""));
        assertThrows(InvalidCredentialException.class, () -> validator.roles(null));
    }
}
