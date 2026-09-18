package trd.home.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import trd.home.auth.constant.UserRole;
import trd.home.auth.exception.InvalidCredentialException;
import trd.home.auth.repository.UserRepository;
import trd.home.common.logging.LogMethodCall;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @LogMethodCall
    public UserDetails loadUserByUsername(String username) {
        var user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialException("Invalid username"));

        String[] roles = user.getRoles().stream().map(this::roleName).toArray(String[]::new);
        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles)
                .build();
    }

    private String roleName(UserRole role) {
        if (role == null) {
            throw new InvalidCredentialException("User role must not be null");
        }
        return role.name();
    }
}
