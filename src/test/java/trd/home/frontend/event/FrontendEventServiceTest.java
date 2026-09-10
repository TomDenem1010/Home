package trd.home.frontend.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import trd.home.common.dto.FrontendEvent;
import trd.home.common.event.FrontendNotificationType;

class FrontendEventServiceTest {

    private final FrontendEventService service = new FrontendEventService();

    @Test
    void subscribesAndSendsToConnectedUser() {
        service.subscribe("alice", "session-1");

        assertTrue(service.hasConnection("alice"));
        assertTrue(service.sendToUser("alice", new FrontendEvent("alice", FrontendNotificationType.SUCCESS, "Done")));
        assertFalse(service.hasConnection("bob"));
        assertFalse(service.sendToUser("bob", new FrontendEvent("bob", FrontendNotificationType.SUCCESS, "Done")));
    }

    @Test
    void removesConnectionsBelongingToDestroyedSession() {
        service.subscribe("alice", "session-1");
        service.subscribe("bob", "session-2");
        SessionDestroyedEvent event = mock(SessionDestroyedEvent.class);
        when(event.getId()).thenReturn("session-1");

        service.closeExpiredSession(event);

        assertFalse(service.hasConnection("alice"));
        assertTrue(service.hasConnection("bob"));
    }

    @Test
    void keepsOtherConnectionForSameUserWhenOneSessionIsDestroyed() throws Exception {
        service.subscribe("alice", "session-1");
        SseEmitter remainingEmitter = service.subscribe("alice", "session-2");
        EmitterHarness remaining = new EmitterHarness(remainingEmitter);
        SessionDestroyedEvent firstSession = mock(SessionDestroyedEvent.class);
        when(firstSession.getId()).thenReturn("session-1");

        service.closeExpiredSession(firstSession);
        assertTrue(service.hasConnection("alice"));

        SessionDestroyedEvent secondSession = mock(SessionDestroyedEvent.class);
        when(secondSession.getId()).thenReturn("session-2");
        service.closeExpiredSession(secondSession);
        assertFalse(service.hasConnection("alice"));
        assertEquals(1, remaining.completeCalls.get());
    }

    @Test
    void sendsHeartbeatWithoutRemovingUsableConnection() {
        service.subscribe("alice", "session-1");

        service.sendHeartbeats();

        assertTrue(service.hasConnection("alice"));
    }

    @Test
    void removesConnectionWhenEmitterCompletesTimesOutOrFails() throws Exception {
        for (String callback : new String[] {"completion", "timeout", "error"}) {
            FrontendEventService testedService = new FrontendEventService();
            SseEmitter emitter = testedService.subscribe("alice", callback);
            EmitterHarness harness = new EmitterHarness(emitter);

            harness.invoke(callback);

            assertFalse(testedService.hasConnection("alice"), callback);
        }
    }

    @Test
    void removesBrokenConnectionAfterEventSendFailure() throws Exception {
        SseEmitter emitter = service.subscribe("alice", "session-1");
        EmitterHarness harness = new EmitterHarness(emitter);
        harness.failWrites = true;

        assertFalse(service.sendToUser("alice", new FrontendEvent("alice", FrontendNotificationType.SUCCESS, "Done")));
        assertFalse(service.hasConnection("alice"));
    }

    @Test
    void removesBrokenConnectionAfterHeartbeatFailure() throws Exception {
        SseEmitter emitter = service.subscribe("alice", "session-1");
        EmitterHarness harness = new EmitterHarness(emitter);
        harness.failWrites = true;

        service.sendHeartbeats();

        assertFalse(service.hasConnection("alice"));
    }

    @Test
    void removesConnectionWhenInitialHandshakeCannotBeSent() throws Exception {
        try (MockedConstruction<SseEmitter> emitters =
                mockConstruction(SseEmitter.class, (emitter, context) -> doThrow(new IOException("disconnected"))
                        .when(emitter)
                        .send(any(SseEmitter.SseEventBuilder.class)))) {
            SseEmitter emitter = service.subscribe("alice", "session-1");

            assertFalse(service.hasConnection("alice"));
            verify(emitter).completeWithError(any(IOException.class));
        }
    }

    private static final class EmitterHarness {
        private final AtomicReference<Runnable> timeout = new AtomicReference<>();
        private final AtomicReference<Runnable> completion = new AtomicReference<>();
        private final AtomicReference<Consumer<Throwable>> error = new AtomicReference<>();
        private final AtomicInteger completeCalls = new AtomicInteger();
        private boolean failWrites;

        private EmitterHarness(SseEmitter emitter) throws Exception {
            Class<?> handlerType =
                    Class.forName("org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter$Handler");
            Object handler = Proxy.newProxyInstance(
                    handlerType.getClassLoader(), new Class<?>[] {handlerType}, (proxy, method, arguments) -> {
                        switch (method.getName()) {
                            case "send" -> {
                                if (failWrites) throw new IOException("disconnected");
                            }
                            case "onTimeout" -> timeout.set((Runnable) arguments[0]);
                            case "onCompletion" -> completion.set((Runnable) arguments[0]);
                            case "onError" -> error.set((Consumer<Throwable>) arguments[0]);
                            case "complete" -> completeCalls.incrementAndGet();
                            default -> {}
                        }
                        return null;
                    });
            Method initialize = emitter.getClass().getSuperclass().getDeclaredMethod("initialize", handlerType);
            initialize.setAccessible(true);
            initialize.invoke(emitter, handler);
        }

        private void invoke(String callback) {
            switch (callback) {
                case "timeout" -> timeout.get().run();
                case "completion" -> completion.get().run();
                case "error" -> error.get().accept(new IOException("disconnected"));
                default -> throw new IllegalArgumentException(callback);
            }
        }
    }
}
