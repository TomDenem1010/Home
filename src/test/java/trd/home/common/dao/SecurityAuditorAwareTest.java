package trd.home.common.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityAuditorAwareTest {

    private final SecurityAuditorAware auditorAware = new SecurityAuditorAware();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsAuthenticatedUsername() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        "alice", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        assertEquals("alice", auditorAware.getCurrentAuditor().orElseThrow());
    }

    @Test
    void returnsSystemWithoutAuthentication() {
        assertEquals("system", auditorAware.getCurrentAuditor().orElseThrow());
    }

    @Test
    void returnsSystemForAnonymousAuthentication() {
        SecurityContextHolder.getContext()
                .setAuthentication(new AnonymousAuthenticationToken(
                        "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertEquals("system", auditorAware.getCurrentAuditor().orElseThrow());
    }
}
