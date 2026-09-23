package trd.home.frontend.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class FrontendEventControllerTest {

    private final FrontendEventService eventService = mock(FrontendEventService.class);
    private final FrontendEventController controller = new FrontendEventController(eventService);

    @Test
    void subscribesAuthenticatedUserAndSession() {
        Principal principal = mock(Principal.class);
        HttpSession session = mock(HttpSession.class);
        SseEmitter emitter = new SseEmitter();
        when(principal.getName()).thenReturn("alice");
        when(session.getId()).thenReturn("session-1");
        when(eventService.subscribe("alice", "session-1")).thenReturn(emitter);

        assertSame(emitter, controller.subscribe(principal, session));
        verify(eventService).subscribe("alice", "session-1");
    }

    @Test
    void returnsServiceAcknowledgementResult() {
        Principal alice = () -> "alice";
        when(eventService.acknowledge("accepted", "alice")).thenReturn(true);
        when(eventService.acknowledge("missing", "alice")).thenReturn(false);

        assertEquals(
                HttpStatus.NO_CONTENT, controller.acknowledge("accepted", alice).getStatusCode());
        assertEquals(
                HttpStatus.NOT_FOUND, controller.acknowledge("missing", alice).getStatusCode());
        verify(eventService).acknowledge("accepted", "alice");
        verify(eventService).acknowledge("missing", "alice");
    }
}
