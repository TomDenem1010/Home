package trd.home.auth.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import trd.home.auth.constant.UserRole;
import trd.home.auth.exception.InvalidCredentialException;
import trd.home.auth.repository.UserRepository;

class InitialAdminUserInitializerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserService userService = mock(UserService.class);
    private final ApplicationArguments arguments = mock(ApplicationArguments.class);

    @Test
    void createsInitialAdminWhenNoAdminExists() {
        InitialAdminUserInitializer initializer = initializer("admin", "strong-password");

        initializer.run(arguments);

        verify(userService).save("admin", "strong-password", Set.of(UserRole.ADMIN));
    }

    @Test
    void leavesExistingAdminUntouched() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);
        InitialAdminUserInitializer initializer = initializer("admin", "strong-password");

        initializer.run(arguments);

        verify(userService, never()).save("admin", "strong-password", Set.of(UserRole.ADMIN));
    }

    @Test
    void requiresUsernameWhenNoAdminExists() {
        InitialAdminUserInitializer initializer = initializer(" ", "strong-password");

        assertThrows(InvalidCredentialException.class, () -> initializer.run(arguments));
        verify(userService, never()).save(" ", "strong-password", Set.of(UserRole.ADMIN));
    }

    @Test
    void requiresPasswordWhenNoAdminExists() {
        InitialAdminUserInitializer initializer = initializer("admin", " ");

        assertThrows(InvalidCredentialException.class, () -> initializer.run(arguments));
        verify(userService, never()).save("admin", " ", Set.of(UserRole.ADMIN));
    }

    private InitialAdminUserInitializer initializer(String username, String password) {
        return new InitialAdminUserInitializer(userRepository, userService, username, password);
    }
}
