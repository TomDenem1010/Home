package trd.home.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import trd.home.common.dao.ApplicationLog;
import trd.home.common.repository.ApplicationLogRepository;

class AuthenticationEventLoggerTest {

    private final ApplicationLogRepository repository = mock(ApplicationLogRepository.class);
    private final AuthenticationEventLogger eventLogger = new AuthenticationEventLogger(repository);

    @Test
    void logsSuccessfulLogin() {
        eventLogger.logLoginSuccess(new AuthenticationSuccessEvent(authentication()));

        ApplicationLog log = savedLog();
        assertEquals("LOGIN", log.getMethod());
        assertEquals("user", log.getInput());
        assertNull(log.getOutput());
        assertNull(log.getError());
        assertEquals(0L, log.getDurationMs());
    }

    @Test
    void logsFailedLogin() {
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");
        eventLogger.logLoginFailure(new AuthenticationFailureBadCredentialsEvent(authentication(), exception));

        ApplicationLog log = savedLog();
        assertEquals("LOGIN", log.getMethod());
        assertEquals("user", log.getInput());
        assertNull(log.getOutput());
        assertEquals(
                "org.springframework.security.authentication.BadCredentialsException: Bad credentials", log.getError());
        assertEquals(0L, log.getDurationMs());
    }

    @Test
    void logsSuccessfulLogout() {
        eventLogger.logLogoutSuccess(new LogoutSuccessEvent(authentication()));

        ApplicationLog log = savedLog();
        assertEquals("LOGOUT", log.getMethod());
        assertEquals("user", log.getInput());
        assertNull(log.getOutput());
        assertNull(log.getError());
        assertEquals(0L, log.getDurationMs());
    }

    private TestingAuthenticationToken authentication() {
        return new TestingAuthenticationToken("user", "password");
    }

    private ApplicationLog savedLog() {
        ArgumentCaptor<ApplicationLog> captor = ArgumentCaptor.forClass(ApplicationLog.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }
}
