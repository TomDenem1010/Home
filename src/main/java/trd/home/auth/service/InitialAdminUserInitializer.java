package trd.home.auth.service;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import trd.home.auth.constant.UserRole;
import trd.home.auth.repository.UserRepository;
import trd.home.common.logging.LogMethodCall;

@Slf4j
@Component
public class InitialAdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final AuthInputValidator validator;
    private final String username;
    private final String password;

    public InitialAdminUserInitializer(
            UserRepository userRepository,
            AuthService authService,
            AuthInputValidator validator,
            @Value("${home.auth.initial-admin.username}") String username,
            @Value("${home.auth.initial-admin.password}") String password) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.validator = validator;
        this.username = username;
        this.password = password;
    }

    @Override
    @LogMethodCall
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            return;
        }

        log.info("Creating initial admin user.");
        validator.credentials(username, password);

        authService.save(username, password, Set.of(UserRole.ADMIN));
        log.info("Initial admin user created successfully.");
    }
}
