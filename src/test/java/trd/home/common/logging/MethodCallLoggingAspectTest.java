package trd.home.common.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;

class MethodCallLoggingAspectTest {

    private final MethodCallLoggingAspect aspect = new MethodCallLoggingAspect();
    private final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
    private final Signature signature = mock(Signature.class);

    @Test
    void logsMethodInputAndOutput() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toLongString()).thenReturn("String Example.find(String)");
        when(joinPoint.getArgs()).thenReturn(new Object[] {"card"});
        when(joinPoint.proceed()).thenReturn("result");

        String output = captureOutput(() -> assertEquals("result", aspect.logMethodCall(joinPoint)));

        assertTrue(output.contains("method=String Example.find(String)"));
        assertTrue(output.contains("input=[card]"));
        assertTrue(output.contains("output=result"));
        assertTrue(output.matches("(?s).*durationMs=\\d+.*"));
    }

    @Test
    void logsAndRethrowsError() throws Throwable {
        IllegalStateException error = new IllegalStateException("failed");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toLongString()).thenReturn("void Example.save() ");
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.proceed()).thenThrow(error);

        String output = captureOutput(() ->
                assertEquals(error, assertThrows(IllegalStateException.class, () -> aspect.logMethodCall(joinPoint))));

        assertTrue(output.contains("input=[]"));
        assertTrue(output.contains("error=java.lang.IllegalStateException: failed"));
        assertTrue(output.matches("(?s).*durationMs=\\d+.*"));
    }

    private String captureOutput(ThrowingRunnable invocation) throws Throwable {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream printStream = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(printStream);
            invocation.run();
        } finally {
            System.setOut(originalOut);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
