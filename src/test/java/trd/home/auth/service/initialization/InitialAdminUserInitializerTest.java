package trd.home.auth.service.initialization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dao.User;
import trd.home.auth.exception.InvalidCredentialException;
import trd.home.auth.repository.UserRepository;
import trd.home.auth.service.AuthService;
import trd.home.auth.service.validation.AuthInputValidator;

class InitialAdminUserInitializerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthService authService = mock(AuthService.class);
    private final ApplicationArguments arguments = mock(ApplicationArguments.class);

    @Test
    void createsConfiguredAdminWhenThatUsernameDoesNotExist() {
        InitialAdminUserInitializer initializer = initializer("admin", "strong-password");

        assertDoesNotThrow(() -> initializer.run(arguments));

        verify(authService).save("admin", "strong-password", Set.of(UserRole.ADMIN));
    }

    @Test
    void leavesConfiguredAdminUntouched() {
        User admin = user(Set.of(UserRole.MEDIA));
        admin.setRoles(Set.of(UserRole.ADMIN));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        InitialAdminUserInitializer initializer = initializer("admin", "strong-password");

        assertDoesNotThrow(() -> initializer.run(arguments));

        verify(authService, never()).save("admin", "strong-password", Set.of(UserRole.ADMIN));
        verify(userRepository, never()).save(admin);
    }

    @Test
    void grantsAdminRoleToExistingConfiguredUser() {
        User user = user(Set.of(UserRole.MEDIA));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        InitialAdminUserInitializer initializer = initializer("admin", "strong-password");

        assertDoesNotThrow(() -> initializer.run(arguments));

        verify(userRepository).save(user);
        verify(authService, never()).save("admin", "strong-password", Set.of(UserRole.ADMIN));
        assertEquals(Set.of(UserRole.MEDIA, UserRole.ADMIN), user.getRoles());
    }

    @Test
    void requiresUsernameWhenNoAdminExists() {
        InitialAdminUserInitializer initializer = initializer(" ", "strong-password");

        assertThrows(InvalidCredentialException.class, () -> initializer.run(arguments));
        verify(authService, never()).save(" ", "strong-password", Set.of(UserRole.ADMIN));
    }

    @Test
    void requiresPasswordWhenNoAdminExists() {
        InitialAdminUserInitializer initializer = initializer("admin", " ");

        assertThrows(InvalidCredentialException.class, () -> initializer.run(arguments));
        verify(authService, never()).save("admin", " ", Set.of(UserRole.ADMIN));
    }

    private InitialAdminUserInitializer initializer(String username, String password) {
        return new InitialAdminUserInitializer(
                userRepository, authService, new AuthInputValidator(), username, password);
    }

    private static User user(Set<UserRole> roles) {
        User user = new User();
        user.setRoles(roles);
        return user;
    }
}
