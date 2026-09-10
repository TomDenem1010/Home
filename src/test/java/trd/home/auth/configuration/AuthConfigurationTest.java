package trd.home.auth.configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;

class AuthConfigurationTest {

    private final AuthConfiguration configuration = new AuthConfiguration();

    @Test
    void createsSecurityInfrastructureBeans() {
        assertInstanceOf(BCryptPasswordEncoder.class, configuration.passwordEncoder());
        assertNotNull(configuration.sessionRegistry());
        assertNotNull(configuration.httpSessionEventPublisher());
    }

    @Test
    void buildsConfiguredSecurityFilterChain() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, Answers.RETURNS_SELF);
        SessionRegistry sessions = mock(SessionRegistry.class);
        DefaultSecurityFilterChain chain = mock(DefaultSecurityFilterChain.class);
        when(http.build()).thenReturn(chain);

        assertSame(chain, configuration.securityFilterChain(http, sessions));
        verify(http).authorizeHttpRequests(any(Customizer.class));
        verify(http).formLogin(any(Customizer.class));
        verify(http).logout(any(Customizer.class));
        verify(http).sessionManagement(any(Customizer.class));
    }
}
