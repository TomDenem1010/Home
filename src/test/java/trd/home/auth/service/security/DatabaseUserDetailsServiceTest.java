package trd.home.auth.service.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dao.User;
import trd.home.auth.exception.InvalidCredentialException;
import trd.home.auth.repository.UserRepository;

class DatabaseUserDetailsServiceTest {
    @Test
    void rejectsNullRoleInStoredUser() {
        var user = new User();
        var roles = new HashSet<UserRole>();
        roles.add(null);
        user.setRoles(roles);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        var exception = assertThrows(InvalidCredentialException.class, () -> service.loadUserByUsername("alice"));
        assertEquals("User role must not be null", exception.getMessage());
    }

    private final UserRepository userRepository = mock(UserRepository.class);
    private final DatabaseUserDetailsService service = new DatabaseUserDetailsService(userRepository);

    @Test
    void loadsUserAndMapsRolesToAuthorities() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("hashed-password");
        user.setRoles(new HashSet<>(Set.of(UserRole.ADMIN, UserRole.TCG)));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("alice");

        assertEquals("alice", details.getUsername());
        assertEquals("hashed-password", details.getPassword());
        assertEquals(
                Set.of("ROLE_ADMIN", "ROLE_TCG"),
                details.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .collect(Collectors.toSet()));
    }

    @Test
    void rejectsUnknownUsername() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("unknown"));
    }
}
