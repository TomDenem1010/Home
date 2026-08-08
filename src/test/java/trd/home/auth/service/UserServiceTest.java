package trd.home.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dao.User;
import trd.home.auth.dto.UserDto;
import trd.home.auth.exception.InvalidCredentialException;
import trd.home.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void returnsEveryAvailableRole() {
        assertEquals(Set.of(UserRole.ADMIN, UserRole.TCG), userService.getAvailableRoles());
    }

    @Test
    void returnsAllUsersAsDtos() {
        User alice = user("alice", "hash-1", Set.of(UserRole.ADMIN));
        alice.setId("user-1");
        User bob = user("bob", "hash-2", Set.of(UserRole.TCG));
        bob.setId("user-2");
        when(userRepository.findAll()).thenReturn(List.of(alice, bob));

        List<UserDto> users = userService.getAllUsers();

        assertEquals(
                List.of(
                        new UserDto("user-1", "alice", Set.of(UserRole.ADMIN)),
                        new UserDto("user-2", "bob", Set.of(UserRole.TCG))),
                users);
    }

    @Test
    void hashesPasswordAndSavesUserWithRoles() {
        Set<UserRole> roles = new HashSet<>(Set.of(UserRole.ADMIN, UserRole.TCG));
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("user-1");
            return user;
        });

        UserDto userDto = userService.save("alice", "plain-password", roles);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User persistedUser = userCaptor.getValue();
        assertEquals("alice", persistedUser.getUsername());
        assertEquals("hashed-password", persistedUser.getPassword());
        assertEquals(roles, persistedUser.getRoles());
        assertNotSame(roles, persistedUser.getRoles());
        assertEquals(UserDto.from(persistedUser), userDto);
        assertEquals("user-1", userDto.id());
        verify(passwordEncoder).encode("plain-password");
    }

    @Test
    void rejectsUsernameThatAlreadyExists() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        InvalidCredentialException exception = assertThrows(
                InvalidCredentialException.class,
                () -> userService.save("alice", "plain-password", Set.of(UserRole.TCG)));

        assertEquals("Invalid username", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void returnsRolesWhenCredentialsAreValid() {
        User user = user("alice", "hashed-password", Set.of(UserRole.TCG));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);

        Set<UserRole> roles = userService.authenticate("alice", "plain-password");

        assertEquals(Set.of(UserRole.TCG), roles);
        verify(passwordEncoder).matches("plain-password", "hashed-password");
    }

    @Test
    void returnsEmptySetWhenPasswordIsInvalid() {
        User user = user("alice", "hashed-password", Set.of(UserRole.ADMIN));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        Set<UserRole> roles = userService.authenticate("alice", "wrong-password");

        assertTrue(roles.isEmpty());
    }

    @Test
    void returnsEmptySetWhenUserDoesNotExist() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Set<UserRole> roles = userService.authenticate("unknown", "plain-password");

        assertTrue(roles.isEmpty());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void updatesRolesByUserId() {
        User user = user("alice", "hashed-password", Set.of(UserRole.TCG));
        user.setId("user-1");
        Set<UserRole> newRoles = new HashSet<>(Set.of(UserRole.ADMIN, UserRole.TCG));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDto result = userService.updateRoles("user-1", newRoles);

        assertEquals(new UserDto("user-1", "alice", Set.of(UserRole.ADMIN, UserRole.TCG)), result);
        assertEquals(newRoles, user.getRoles());
        assertNotSame(newRoles, user.getRoles());
        verify(userRepository).save(user);
    }

    @Test
    void rejectsRoleUpdateForUnknownUserId() {
        when(userRepository.findById("unknown-id")).thenReturn(Optional.empty());

        InvalidCredentialException exception = assertThrows(
                InvalidCredentialException.class, () -> userService.updateRoles("unknown-id", Set.of(UserRole.TCG)));

        assertEquals("Invalid user id", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsNullRolesWhenUpdating() {
        assertThrows(InvalidCredentialException.class, () -> userService.updateRoles("user-1", null));
        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @Test
    void hashesAndUpdatesPasswordByUserId() {
        User user = user("alice", "old-hash", Set.of(UserRole.TCG));
        user.setId("user-1");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(userRepository.save(user)).thenReturn(user);

        UserDto result = userService.updatePassword("user-1", "new-password");

        assertEquals("new-hash", user.getPassword());
        assertEquals(new UserDto("user-1", "alice", Set.of(UserRole.TCG)), result);
        verify(passwordEncoder).encode("new-password");
        verify(userRepository).save(user);
    }

    @Test
    void rejectsPasswordUpdateForUnknownUserId() {
        when(userRepository.findById("unknown-id")).thenReturn(Optional.empty());

        InvalidCredentialException exception = assertThrows(
                InvalidCredentialException.class, () -> userService.updatePassword("unknown-id", "new-password"));

        assertEquals("Invalid user id", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void rejectsInvalidPasswordWhenUpdating(String password) {
        assertThrows(InvalidCredentialException.class, () -> userService.updatePassword("user-1", password));
        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void rejectsInvalidUsername(String username) {
        assertThrows(InvalidCredentialException.class, () -> userService.authenticate(username, "plain-password"));
        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void rejectsInvalidPassword(String password) {
        assertThrows(InvalidCredentialException.class, () -> userService.authenticate("alice", password));
        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @Test
    void rejectsNullRolesWhenSaving() {
        assertThrows(InvalidCredentialException.class, () -> userService.save("alice", "plain-password", null));
        verifyNoInteractions(passwordEncoder, userRepository);
    }

    private static User user(String username, String password, Set<UserRole> roles) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRoles(new HashSet<>(roles));
        return user;
    }
}
