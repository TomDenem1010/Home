package trd.home.auth.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final SessionRegistry sessionRegistry;

    public void expireSessions(String username) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> UserDetails.class.isInstance(principal))
                .map(principal -> UserDetails.class.cast(principal))
                .filter(principal -> principal.getUsername().equals(username))
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .forEach(session -> session.expireNow());
    }
}
