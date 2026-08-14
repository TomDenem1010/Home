package trd.home.common.logging;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodCallLoggingAspect {

    @Around("@annotation(trd.home.common.logging.LogMethodCall)")
    public Object logMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toLongString();
        String input = Arrays.deepToString(joinPoint.getArgs());
        long startedAt = System.nanoTime();

        try {
            Object output = joinPoint.proceed();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            System.out.printf("method=%s, input=%s, output=%s, durationMs=%d%n", method, input, output, durationMs);
            return output;
        } catch (Throwable throwable) {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            System.out.printf("method=%s, input=%s, error=%s, durationMs=%d%n", method, input, throwable, durationMs);
            throw throwable;
        }
    }
}
