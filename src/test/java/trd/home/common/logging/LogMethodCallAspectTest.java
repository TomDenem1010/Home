package trd.home.common.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import trd.home.common.dao.ApplicationLog;
import trd.home.common.repository.ApplicationLogRepository;

class LogMethodCallAspectTest {

    private final ApplicationLogRepository repository = mock(ApplicationLogRepository.class);
    private final LogMethodCallAspect aspect = new LogMethodCallAspect(repository);
    private final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    private final MethodSignature signature = mock(MethodSignature.class);

    @Test
    void logsMethodInputAndOutput() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toLongString()).thenReturn("String Example.find(String)");
        when(joinPoint.getArgs()).thenReturn(new Object[] {"card"});
        when(joinPoint.proceed()).thenReturn("result");

        assertEquals("result", aspect.logMethodCall(joinPoint));

        ApplicationLog log = savedLog();
        assertEquals("String Example.find(String)", log.getMethod());
        assertEquals("[card]", log.getInput());
        assertEquals("result", log.getOutput());
        assertNull(log.getError());
    }

    @Test
    void logsAndRethrowsError() throws Throwable {
        IllegalStateException error = new IllegalStateException("failed");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toLongString()).thenReturn("void Example.save() ");
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.proceed()).thenThrow(error);

        assertEquals(error, assertThrows(IllegalStateException.class, () -> aspect.logMethodCall(joinPoint)));

        ApplicationLog log = savedLog();
        assertEquals("void Example.save() ", log.getMethod());
        assertNull(log.getInput());
        assertNull(log.getOutput());
        assertEquals("java.lang.IllegalStateException: failed", log.getError());
    }

    @Test
    void masksAnnotatedArguments() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toLongString()).thenReturn("void Example.save(String, String)");
        when(signature.getMethod()).thenReturn(Example.class.getDeclaredMethod("save", String.class, String.class));
        when(joinPoint.getArgs()).thenReturn(new Object[] {"alice", "secret-password"});

        aspect.logMethodCall(joinPoint);

        assertEquals("[alice, ***]", savedLog().getInput());
    }

    private ApplicationLog savedLog() {
        ArgumentCaptor<ApplicationLog> captor = ArgumentCaptor.forClass(ApplicationLog.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    private static class Example {

        @SuppressWarnings("unused")
        void save(String username, @LogMasked String password) {}
    }
}
