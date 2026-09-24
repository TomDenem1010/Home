package trd.home.auth.service.initialization;

import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dao.User;
import trd.home.auth.repository.UserRepository;
import trd.home.auth.service.AuthService;
import trd.home.auth.service.validation.AuthInputValidator;

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
    public void run(ApplicationArguments args) {
        User existingUser = userRepository.findByUsername(username).orElse(null);
        if (existingUser != null) {
            grantAdminRoleIfMissing(existingUser);
            return;
        }

        log.info("Creating initial admin user.");
        validator.credentials(username, password);

        authService.save(username, password, Set.of(UserRole.ADMIN));
        log.info("Initial admin user created successfully.");
    }

    private void grantAdminRoleIfMissing(User user) {
        if (user.getRoles().contains(UserRole.ADMIN)) {
            return;
        }

        log.info("Granting admin role to initial admin user.");
        Set<UserRole> roles = new HashSet<>(user.getRoles());
        roles.add(UserRole.ADMIN);
        user.setRoles(roles);
        userRepository.save(user);
        log.info("Admin role granted to initial admin user successfully.");
    }
}
