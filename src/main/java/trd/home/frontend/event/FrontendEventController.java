package trd.home.frontend.event;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import trd.home.common.logging.LogMethodCall;

@RestController
@RequestMapping("/api/frontend-events")
@RequiredArgsConstructor
public class FrontendEventController {

    private final FrontendEventService eventService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @LogMethodCall
    public SseEmitter subscribe(Principal principal, HttpSession session) {
        return eventService.subscribe(principal.getName(), session.getId());
    }
}
