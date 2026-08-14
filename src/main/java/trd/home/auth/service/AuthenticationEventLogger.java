package trd.home.auth.service;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;
import trd.home.auth.constant.LoginType;
import trd.home.common.dao.ApplicationLog;
import trd.home.common.repository.ApplicationLogRepository;

@Component
public class AuthenticationEventLogger {

    private static final long NO_DURATION = 0L;

    private final ApplicationLogRepository applicationLogRepository;

    public AuthenticationEventLogger(ApplicationLogRepository applicationLogRepository) {
        this.applicationLogRepository = applicationLogRepository;
    }

    @EventListener
    public void logLoginSuccess(AuthenticationSuccessEvent event) {
        save(LoginType.LOGIN_SUCCESS, event.getAuthentication().getName());
    }

    @EventListener
    public void logLoginFailure(AbstractAuthenticationFailureEvent event) {
        applicationLogRepository.save(ApplicationLog.failed(
                LoginType.LOGIN_FAILURE.getPath(),
                event.getAuthentication().getName(),
                event.getException(),
                NO_DURATION));
    }

    @EventListener
    public void logLogoutSuccess(LogoutSuccessEvent event) {
        save(LoginType.LOGOUT_SUCCESS, event.getAuthentication().getName());
    }

    private void save(LoginType loginType, String username) {
        applicationLogRepository.save(ApplicationLog.successful(loginType.getPath(), username, null, NO_DURATION));
    }
}
