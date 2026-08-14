package trd.home.common.logging;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import trd.home.common.dao.ApplicationLog;
import trd.home.common.repository.ApplicationLogRepository;

@Aspect
@Component
public class MethodCallLoggingAspect {

    private final ApplicationLogRepository applicationLogRepository;

    public MethodCallLoggingAspect(ApplicationLogRepository applicationLogRepository) {
        this.applicationLogRepository = applicationLogRepository;
    }

    @Around("@annotation(trd.home.common.logging.LogMethodCall)")
    public Object logMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toLongString();
        String input = Arrays.deepToString(joinPoint.getArgs());
        long startedAt = System.nanoTime();

        Object output;
        try {
            output = joinPoint.proceed();
        } catch (Throwable throwable) {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            applicationLogRepository.save(ApplicationLog.failed(method, input, throwable, durationMs));
            throw throwable;
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        applicationLogRepository.save(ApplicationLog.successful(method, input, output, durationMs));
        return output;
    }
}
