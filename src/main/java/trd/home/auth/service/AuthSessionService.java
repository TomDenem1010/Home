package trd.home.auth.service;

import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthSessionService {

    private final SessionRegistry sessionRegistry;

    public AuthSessionService(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void expireSessions(String username) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(UserDetails.class::isInstance)
                .map(UserDetails.class::cast)
                .filter(principal -> principal.getUsername().equals(username))
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .forEach(session -> session.expireNow());
    }
}
