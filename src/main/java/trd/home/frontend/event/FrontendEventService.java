package trd.home.frontend.event;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import trd.home.common.dto.FrontendEvent;

@Slf4j
@Service
public class FrontendEventService {

    private static final long NO_SERVER_TIMEOUT = 0L;

    private final ConcurrentHashMap<String, Set<Connection>> connectionsByUsername = new ConcurrentHashMap<>();

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

    public void sendToUser(String username, FrontendEvent event) {
        Set<Connection> connections = connectionsByUsername.get(username);
        if (connections == null || connections.isEmpty()) {
            return;
        }
        connections.stream().anyMatch(connection -> send(username, connection, event));
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

    private boolean send(String username, Connection connection, FrontendEvent event) {
        try {
            connection.emitter().send(SseEmitter.event().name("notification").data(event));
            return true;
        } catch (IOException | IllegalStateException exception) {
            log.error("Failed to send frontend event to user '{}'", username, exception);
            remove(username, connection);
            connection.emitter().complete();
            return false;
        }
    }

    private void remove(String username, Connection connection) {
        connectionsByUsername.computeIfPresent(username, (ignored, connections) -> {
            connections.remove(connection);
            return connections.isEmpty() ? null : connections;
        });
    }

    private record Connection(String sessionId, SseEmitter emitter) {}
}
