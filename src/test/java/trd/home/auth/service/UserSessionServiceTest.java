package trd.home.auth.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;

class UserSessionServiceTest {

    private final SessionRegistry sessionRegistry = mock(SessionRegistry.class);
    private final UserSessionService service = new UserSessionService(sessionRegistry);

    @Test
    void expiresEveryActiveSessionForUsername() {
        var alice = User.withUsername("alice").password("hash").roles("TCG").build();
        var bob = User.withUsername("bob").password("hash").roles("TCG").build();
        var firstAliceSession = new SessionInformation(alice, "alice-1", new Date());
        var secondAliceSession = new SessionInformation(alice, "alice-2", new Date());
        var bobSession = new SessionInformation(bob, "bob-1", new Date());
        when(sessionRegistry.getAllPrincipals()).thenReturn(List.of(alice, bob));
        when(sessionRegistry.getAllSessions(alice, false)).thenReturn(List.of(firstAliceSession, secondAliceSession));
        when(sessionRegistry.getAllSessions(bob, false)).thenReturn(List.of(bobSession));

        service.expireSessions("alice");

        assertTrue(firstAliceSession.isExpired());
        assertTrue(secondAliceSession.isExpired());
        assertFalse(bobSession.isExpired());
    }
}
