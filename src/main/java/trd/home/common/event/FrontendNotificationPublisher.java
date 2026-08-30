package trd.home.common.event;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.dto.FrontendEvent;
import trd.home.common.exception.UnableToSerializeNotificationException;
import trd.home.common.logging.LogMethodCall;
import trd.home.common.repository.ApplicationEventRepository;

@Slf4j
@Service
public class FrontendNotificationPublisher {

    private final ApplicationEventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final AuditorAware<String> auditorAware;

    public FrontendNotificationPublisher(
            ApplicationEventRepository eventRepository, ObjectMapper objectMapper, AuditorAware<String> auditorAware) {
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.auditorAware = auditorAware;
    }

    @LogMethodCall
    public void publish(FrontendNotificationType type, String message) {
        publish(auditorAware.getCurrentAuditor().orElse("system"), type, message);
    }

    @LogMethodCall
    public void publish(String username, FrontendNotificationType type, String message) {
        try {
            String serializedNotification = objectMapper.writeValueAsString(
                    new FrontendEvent(Objects.requireNonNullElse(username, "system"), type, message));
            eventRepository.save(new ApplicationEvent(EventType.FRONTEND_NOTIFICATION, serializedNotification));
        } catch (JacksonException exception) {
            log.error("Failed to serialize frontend notification for user '{}'", username, exception);
            throw new UnableToSerializeNotificationException("Unable to serialize frontend notification", exception);
        }
    }
}
