package trd.home.auth.service;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trd.home.auth.constant.UserRole;
import trd.home.auth.dao.User;
import trd.home.auth.dto.UserDto;
import trd.home.auth.exception.InvalidCredentialException;
import trd.home.auth.repository.UserRepository;
import trd.home.common.logging.LogMasked;
import trd.home.common.logging.LogMethodCall;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthSessionService userSessionService;
    private final AuthInputValidator validator;

    @LogMethodCall
    public Set<UserRole> getAvailableRoles() {
        return EnumSet.allOf(UserRole.class);
    }

    @LogMethodCall
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserDto::from).toList();
    }

    @LogMethodCall
    @Transactional
    public UserDto save(String username, @LogMasked String password, Set<UserRole> roles) {
        validator.credentials(username, password);
        validator.roles(roles);
        validateUsernameIsAvailable(username);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(new HashSet<>(roles));
        return UserDto.from(userRepository.save(user));
    }

    @LogMethodCall
    @Transactional(readOnly = true)
    public Set<UserRole> authenticate(String username, @LogMasked String password) {
        validator.credentials(username, password);

        return userRepository
                .findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> Set.copyOf(user.getRoles()))
                .orElseGet(Set::of);
    }

    @LogMethodCall
    @Transactional
    public UserDto updateRoles(String userId, Set<UserRole> roles) {
        validator.userId(userId);
        validator.roles(roles);

        User user = findUserById(userId);
        user.setRoles(new HashSet<>(roles));
        UserDto updatedUser = UserDto.from(userRepository.save(user));
        userSessionService.expireSessions(user.getUsername());
        return updatedUser;
    }

    @LogMethodCall
    @Transactional
    public UserDto updatePassword(String userId, @LogMasked String password) {
        validator.userId(userId);
        validator.password(password);

        User user = findUserById(userId);
        user.setPassword(passwordEncoder.encode(password));
        UserDto updatedUser = UserDto.from(userRepository.save(user));
        userSessionService.expireSessions(user.getUsername());
        return updatedUser;
    }

    private void validateUsernameIsAvailable(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new InvalidCredentialException("Invalid username");
        }
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new InvalidCredentialException("Invalid user id"));
    }
}
