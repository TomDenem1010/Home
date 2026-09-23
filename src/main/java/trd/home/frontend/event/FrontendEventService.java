package trd.home.frontend.event;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.dto.FrontendEvent;
import trd.home.common.event.ApplicationEventQueue;

@Slf4j
@Service
public class FrontendEventService {

    private static final long NO_SERVER_TIMEOUT = 0L;

    private final ConcurrentHashMap<String, Set<Connection>> connectionsByUsername = new ConcurrentHashMap<>();
    private final ApplicationEventQueue eventQueue;
    private final ObjectMapper objectMapper;

    public FrontendEventService(ApplicationEventQueue eventQueue, ObjectMapper objectMapper) {
        this.eventQueue = eventQueue;
        this.objectMapper = objectMapper;
    }

    public SseEmitter subscribe(String username, String sessionId) {
        SseEmitter emitter = new SseEmitter(NO_SERVER_TIMEOUT);
        Connection connection = new Connection(sessionId, emitter);
        connectionsByUsername
                .computeIfAbsent(username, ignored -> ConcurrentHashMap.newKeySet())
                .add(connection);

        Runnable removeConnection = () -> remove(username, connection);
        emitter.onCompletion(removeConnection);
        emitter.onTimeout(removeConnection);
        emitter.onError(ignored -> removeConnection.run());

        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException exception) {
            log.error("Failed to establish frontend event stream for user '{}'", username, exception);
            remove(username, connection);
            emitter.completeWithError(exception);
        }
        return emitter;
    }

    public boolean hasConnection(String username) {
        Set<Connection> connections = connectionsByUsername.get(username);
        return connections != null && !connections.isEmpty();
    }

    @Scheduled(fixedDelayString = "${frontend.sse.heartbeat.delay:15s}")
    public void sendHeartbeats() {
        connectionsByUsername.forEach(
                (username, connections) -> connections.forEach(connection -> sendHeartbeat(username, connection)));
    }

    public boolean sendToUser(String username, String eventId, FrontendEvent event) {
        Set<Connection> connections = connectionsByUsername.get(username);
        if (connections == null || connections.isEmpty()) {
            return false;
        }
        return connections.stream().anyMatch(connection -> send(username, connection, eventId, event));
    }

    public boolean acknowledge(String eventId, String username) {
        var event = eventQueue.findById(eventId).orElse(null);
        if (event == null || event.getType() != EventType.FRONTEND_NOTIFICATION) {
            return false;
        }
        FrontendEvent notification;
        try {
            notification = objectMapper.readValue(event.getMessage(), FrontendEvent.class);
        } catch (RuntimeException exception) {
            return false;
        }
        if (!username.equals(notification.username())) {
            return false;
        }
        if (event.getStatus() == EventStatus.TO_DO) {
            event.markDone();
            eventQueue.save(event);
        }
        return true;
    }

    @EventListener
    public void closeExpiredSession(SessionDestroyedEvent event) {
        connectionsByUsername.forEach((username, connections) -> connections.stream()
                .filter(connection -> connection.sessionId().equals(event.getId()))
                .toList()
                .forEach(connection -> {
                    remove(username, connection);
                    connection.emitter().complete();
                }));
    }

    private boolean send(String username, Connection connection, String eventId, FrontendEvent event) {
        return send(
                username,
                connection,
                SseEmitter.event().name("notification").id(eventId).data(event));
    }

    private void sendHeartbeat(String username, Connection connection) {
        send(username, connection, SseEmitter.event().comment("heartbeat"));
    }

    private boolean send(String username, Connection connection, SseEmitter.SseEventBuilder event) {
        try {
            connection.emitter().send(event);
            return true;
        } catch (IOException | IllegalStateException exception) {
            log.debug("Removing disconnected frontend event stream for user '{}'", username, exception);
            remove(username, connection);
            return false;
        }
    }

    private void remove(String username, Connection connection) {
        connectionsByUsername.computeIfPresent(username, (ignored, connections) -> {
            connections.remove(connection);
            return connections.isEmpty() ? null : connections;
        });
    }

    private record Connection(
            @NonNull String sessionId, @NonNull SseEmitter emitter) {}
}
