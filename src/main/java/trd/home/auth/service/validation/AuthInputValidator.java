package trd.home.auth.service.validation;

import java.util.Set;
import org.springframework.stereotype.Component;
import trd.home.auth.constant.UserRole;
import trd.home.auth.exception.InvalidCredentialException;

@Component
public class AuthInputValidator {

    public void credentials(String username, String password) {
        username(username);
        password(password);
    }

    public void username(String username) {
        requireText(username, "Username must not be blank");
    }

    public void password(String password) {
        requireText(password, "Password must not be blank");
    }

    public void userId(String userId) {
        requireText(userId, "User id must not be blank");
    }

    public void roles(Set<UserRole> roles) {
        if (roles == null) {
            throw new InvalidCredentialException("Roles must not be null");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidCredentialException(message);
        }
    }
}
