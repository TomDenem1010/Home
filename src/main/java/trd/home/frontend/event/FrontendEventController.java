package trd.home.frontend.event;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dto.FrontendEvent;
import trd.home.common.logging.LogMethodCall;
import trd.home.common.repository.ApplicationEventRepository;

@RestController
@RequestMapping("/api/frontend-events")
@RequiredArgsConstructor
public class FrontendEventController {

    private final FrontendEventService eventService;
    private final ApplicationEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @LogMethodCall
    public SseEmitter subscribe(Principal principal, HttpSession session) {
        return eventService.subscribe(principal.getName(), session.getId());
    }

    @PostMapping("/{eventId}/ack")
    public ResponseEntity<Void> acknowledge(@PathVariable String eventId, Principal principal) {
        var event = eventRepository.findById(eventId).orElse(null);
        if (event == null || event.getType() != EventType.FRONTEND_NOTIFICATION) {
            return ResponseEntity.notFound().build();
        }
        FrontendEvent notification;
        try {
            notification = objectMapper.readValue(event.getMessage(), FrontendEvent.class);
        } catch (RuntimeException exception) {
            return ResponseEntity.notFound().build();
        }
        if (!principal.getName().equals(notification.username())) {
            return ResponseEntity.notFound().build();
        }
        if (event.getStatus() == EventStatus.TO_DO) {
            event.markDone();
            eventRepository.save(event);
        }
        return ResponseEntity.noContent().build();
    }
}
