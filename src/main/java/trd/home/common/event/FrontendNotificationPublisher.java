package trd.home.common.event;

import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import trd.home.common.constant.EventType;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.dto.FrontendEvent;
import trd.home.common.repository.ApplicationEventRepository;

@Service
public class FrontendNotificationPublisher {

    private final ApplicationEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public FrontendNotificationPublisher(ApplicationEventRepository eventRepository, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    public void publish(FrontendNotificationType type, String message) {
        try {
            String serializedNotification = objectMapper.writeValueAsString(new FrontendEvent(type, message));
            eventRepository.save(new ApplicationEvent(EventType.FRONTEND_NOTIFICATION, serializedNotification));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Unable to serialize frontend notification", exception);
        }
    }
}
