package trd.home.auth.configuration;

import static jakarta.servlet.DispatcherType.ASYNC;
import static jakarta.servlet.DispatcherType.ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    void buildsConfiguredSecurityFilterChain() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class, Answers.RETURNS_SELF);
        SessionRegistry sessions = mock(SessionRegistry.class);
        DefaultSecurityFilterChain chain = mock(DefaultSecurityFilterChain.class);
        when(http.build()).thenReturn(chain);
        var authorize = mock(
                AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class,
                Answers.RETURNS_DEEP_STUBS);
        var form = mock(FormLoginConfigurer.class, Answers.RETURNS_SELF);
        var logout = mock(LogoutConfigurer.class, Answers.RETURNS_SELF);
        var session = mock(SessionManagementConfigurer.class, Answers.RETURNS_DEEP_STUBS);
        doAnswer(invocation -> {
                    invocation.<Customizer>getArgument(0).customize(authorize);
                    return http;
                })
                .when(http)
                .authorizeHttpRequests(any());
        doAnswer(invocation -> {
                    invocation.<Customizer>getArgument(0).customize(form);
                    return http;
                })
                .when(http)
                .formLogin(any());
        doAnswer(invocation -> {
                    invocation.<Customizer>getArgument(0).customize(logout);
                    return http;
                })
                .when(http)
                .logout(any());
        doAnswer(invocation -> {
                    invocation.<Customizer>getArgument(0).customize(session);
                    return http;
                })
                .when(http)
                .sessionManagement(any());

        assertSame(chain, configuration.securityFilterChain(http, sessions));
        verify(http).authorizeHttpRequests(any());
        verify(http).formLogin(any());
        verify(http).logout(any());
        verify(http).sessionManagement(any());
        verify(authorize).dispatcherTypeMatchers(ASYNC, ERROR);
        verify(form).defaultSuccessUrl("/", true);
        verify(form).permitAll();
        verify(logout).logoutSuccessUrl("/login");
        verify(logout).permitAll();
        verify(session.maximumSessions(-1)).sessionRegistry(sessions);
        verify(session.maximumSessions(-1).sessionRegistry(sessions)).expiredUrl("/login?expired");
    }
}
