package trd.home.common.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.lang.reflect.Method;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import trd.home.common.dao.ApplicationLog;
import trd.home.common.repository.ApplicationLogRepository;

class LogMethodCallAspectTest {

    private final ApplicationLogRepository repository = mock(ApplicationLogRepository.class);
    private final LogMethodCallAspect aspect = new LogMethodCallAspect(repository);
    private final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    private final MethodSignature signature = mock(MethodSignature.class);
    private final Logger logger = (Logger) LoggerFactory.getLogger(LogMethodCallAspect.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Level originalLevel;

    @BeforeEach
    void setUp() {
        originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        logger.setLevel(originalLevel);
        appender.stop();
    }

    @Test
    void logsFullMethodInputOutputAndDurationByDefault() throws Throwable {
        Method method = Example.class.getDeclaredMethod("find", List.class);
        prepare(method, new Object[] {List.of("first", "second")}, List.of("result-one", "result-two"));

        assertEquals(List.of("result-one", "result-two"), aspect.logMethodCall(joinPoint, annotation(method)));

        assertEquals("Calling " + method.toGenericString() + " with input [[first, second]]", message(0));
        assertTrue(message(1).matches("Completed .+ with output \\[result-one, result-two] in \\d+ ms"));
        ApplicationLog applicationLog = savedLog();
        assertEquals("[[first, second]]", applicationLog.getInput());
        assertEquals("[result-one, result-two]", applicationLog.getOutput());
    }

    @Test
    void respectsDisabledInputOutputAndDuration() throws Throwable {
        Method method = Example.class.getDeclaredMethod("withoutDetails");
        prepare(method, new Object[0], "result");

        assertEquals("result", aspect.logMethodCall(joinPoint, annotation(method)));

        assertEquals(0, appender.list.size());
        assertEquals("result", savedLog().getOutput());
    }

    @Test
    void skipsDatabaseAuditWhenDisabled() throws Throwable {
        Method method = Example.class.getDeclaredMethod("withoutAudit");
        prepare(method, new Object[0], "result");

        assertEquals("result", aspect.logMethodCall(joinPoint, annotation(method)));

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void skipsFailedDatabaseAuditWhenDisabled() throws Throwable {
        Method method = Example.class.getDeclaredMethod("failWithoutAudit");
        IllegalStateException exception = new IllegalStateException("failed");
        prepareSignature(method, new Object[0]);
        when(joinPoint.proceed()).thenThrow(exception);

        assertThrows(IllegalStateException.class, () -> aspect.logMethodCall(joinPoint, annotation(method)));

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void masksAnnotatedArgumentsWhileLoggingOtherObjectsInFull() throws Throwable {
        Method method = Example.class.getDeclaredMethod("save", String.class, String.class);
        prepare(method, new Object[] {"alice", "secret-password"}, null);

        aspect.logMethodCall(joinPoint, annotation(method));

        assertEquals("Calling " + method.toGenericString() + " with input [alice, ***]", message(0));
        assertEquals("Completed " + method.toGenericString() + " with output null", message(1));
        assertEquals("[alice, ***]", savedLog().getInput());
    }

    @Test
    void logsAndRethrowsFailure() throws Throwable {
        Method method = Example.class.getDeclaredMethod("fail");
        IllegalStateException exception = new IllegalStateException("failed");
        prepareSignature(method, new Object[0]);
        when(joinPoint.proceed()).thenThrow(exception);

        assertEquals(
                exception,
                assertThrows(IllegalStateException.class, () -> aspect.logMethodCall(joinPoint, annotation(method))));
        assertEquals(Level.ERROR, appender.list.get(1).getLevel());
        assertEquals("Logged method call failed: " + method.toGenericString(), message(1));
        assertEquals(
                IllegalStateException.class.getName(),
                appender.list.get(1).getThrowableProxy().getClassName());
        assertEquals("failed", appender.list.get(1).getThrowableProxy().getMessage());
        assertEquals("java.lang.IllegalStateException: failed", savedLog().getError());
    }

    private void prepare(Method method, Object[] arguments, Object output) throws Throwable {
        prepareSignature(method, arguments);
        when(joinPoint.proceed()).thenReturn(output);
    }

    private void prepareSignature(Method method, Object[] arguments) {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toLongString()).thenReturn(method.toGenericString());
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(arguments);
    }

    private LogMethodCall annotation(Method method) {
        return method.getAnnotation(LogMethodCall.class);
    }

    private String message(int index) {
        return appender.list.get(index).getFormattedMessage();
    }

    private ApplicationLog savedLog() {
        ArgumentCaptor<ApplicationLog> captor = ArgumentCaptor.forClass(ApplicationLog.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    private static class Example {

        @LogMethodCall
        List<String> find(List<String> values) {
            return values;
        }

        @LogMethodCall(in = false, out = false, duration = false)
        String withoutDetails() {
            return "result";
        }

        @LogMethodCall(audit = false)
        String withoutAudit() {
            return "result";
        }

        @LogMethodCall(audit = false)
        void failWithoutAudit() {}

        @LogMethodCall(duration = false)
        void save(String username, @LogMasked String password) {}

        @LogMethodCall(out = false, duration = false)
        void fail() {}
    }
}
