package trd.home.frontend.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.repository.ApplicationEventRepository;

class FrontendEventControllerTest {

    private final FrontendEventService eventService = mock(FrontendEventService.class);
    private final ApplicationEventRepository eventRepository = mock(ApplicationEventRepository.class);
    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private final FrontendEventController controller =
            new FrontendEventController(eventService, eventRepository, objectMapper);

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
    void acknowledgesOnlyRecipientsNotification() {
        ApplicationEvent event = new ApplicationEvent(
                EventType.FRONTEND_NOTIFICATION,
                "{\"username\":\"alice\",\"type\":\"WARNING\",\"message\":\"Started\"}");
        when(eventRepository.findById("event-1")).thenReturn(Optional.of(event));
        Principal alice = () -> "alice";
        Principal bob = () -> "bob";

        assertEquals(
                HttpStatus.NOT_FOUND, controller.acknowledge("event-1", bob).getStatusCode());
        assertEquals(EventStatus.TO_DO, event.getStatus());
        assertEquals(
                HttpStatus.NO_CONTENT, controller.acknowledge("event-1", alice).getStatusCode());
        assertEquals(EventStatus.DONE, event.getStatus());
        verify(eventRepository).save(event);
    }
}
