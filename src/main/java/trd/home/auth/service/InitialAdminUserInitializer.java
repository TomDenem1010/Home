package trd.home.auth.service;

import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import trd.home.auth.constant.UserRole;
import trd.home.auth.exception.InvalidCredentialException;
import trd.home.auth.repository.UserRepository;

@Slf4j
@Component
public class InitialAdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final String username;
    private final String password;

    public InitialAdminUserInitializer(
            UserRepository userRepository,
            UserService userService,
            @Value("${home.auth.initial-admin.username}") String username,
            @Value("${home.auth.initial-admin.password}") String password) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            log.info("Initial admin user already exists");
            return;
        }

        validateUsername(username);
        validatePassword(password);

        userService.save(username, password, Set.of(UserRole.ADMIN));
        log.info("Initial admin user created: {}", username);
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
