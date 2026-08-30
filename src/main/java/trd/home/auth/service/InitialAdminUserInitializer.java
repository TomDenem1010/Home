package trd.home.auth.service;

import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import trd.home.auth.constant.UserRole;
import trd.home.auth.exception.InvalidCredentialException;
import trd.home.auth.repository.UserRepository;
import trd.home.common.logging.LogMethodCall;

@Component
public class InitialAdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final String username;
    private final String password;

    public InitialAdminUserInitializer(
            UserRepository userRepository,
            AuthService authService,
            @Value("${home.auth.initial-admin.username}") String username,
            @Value("${home.auth.initial-admin.password}") String password) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.username = username;
        this.password = password;
    }

    @Override
    @LogMethodCall
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            return;
        }

        validateUsername(username);
        validatePassword(password);

        authService.save(username, password, Set.of(UserRole.ADMIN));
    }

    private void validatePassword(String password) {
        if (Objects.isNull(password) || password.isBlank()) {
            throw new InvalidCredentialException("Password must not be blank");
        }
    }

    private void validateUsername(String username) {
        if (Objects.isNull(username) || username.isBlank()) {
            throw new InvalidCredentialException("Username must not be blank");
        }
    }
}
