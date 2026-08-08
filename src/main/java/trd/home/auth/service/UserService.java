package trd.home.auth.service;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dao.User;
import trd.home.auth.dto.UserDto;
import trd.home.auth.exception.InvalidCredentialException;
import trd.home.auth.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserSessionService userSessionService;

    public Set<UserRole> getAvailableRoles() {
        return EnumSet.allOf(UserRole.class);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserDto::from).toList();
    }

    @Transactional
    public UserDto save(String username, String password, Set<UserRole> roles) {
        validateCredentials(username, password);
        validateRoles(roles);
        validateUsernameExistence(username);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(new HashSet<>(roles));
        return UserDto.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Set<UserRole> authenticate(String username, String password) {
        validateCredentials(username, password);

        return userRepository
                .findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> Set.copyOf(user.getRoles()))
                .orElseGet(Set::of);
    }

    @Transactional
    public UserDto updateRoles(String userId, Set<UserRole> roles) {
        validateUserId(userId);
        validateRoles(roles);

        User user = findUserById(userId);
        user.setRoles(new HashSet<>(roles));
        UserDto updatedUser = UserDto.from(userRepository.save(user));
        userSessionService.expireSessions(user.getUsername());
        return updatedUser;
    }

    @Transactional
    public UserDto updatePassword(String userId, String password) {
        validateUserId(userId);
        validatePassword(password);

        User user = findUserById(userId);
        user.setPassword(passwordEncoder.encode(password));
        return UserDto.from(userRepository.save(user));
    }

    private void validateCredentials(String username, String password) {
        validateUsername(username);
        validatePassword(password);
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

    private void validateUserId(String userId) {
        if (Objects.isNull(userId) || userId.isBlank()) {
            throw new InvalidCredentialException("User id must not be blank");
        }
    }

    private void validateRoles(Set<UserRole> roles) {
        if (Objects.isNull(roles)) {
            throw new InvalidCredentialException("Roles must not be null");
        }
    }

    private void validateUsernameExistence(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new InvalidCredentialException("Invalid username");
        }
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new InvalidCredentialException("Invalid user id"));
    }
}
