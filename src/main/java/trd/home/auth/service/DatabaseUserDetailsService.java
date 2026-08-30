package trd.home.auth.service;

import java.util.Objects;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import trd.home.auth.exception.InvalidCredentialException;
import trd.home.auth.repository.UserRepository;
import trd.home.common.logging.LogMethodCall;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @LogMethodCall
    public UserDetails loadUserByUsername(String username) {
        var user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialException("Invalid username"));

        String[] roles = user.getRoles().stream()
                .map(role -> Objects.requireNonNull(role, "User role must not be null")
                        .name())
                .toArray(String[]::new);
        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles)
                .build();
    }
}
