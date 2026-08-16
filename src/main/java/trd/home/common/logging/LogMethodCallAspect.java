package trd.home.common.logging;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import trd.home.common.dao.ApplicationLog;
import trd.home.common.repository.ApplicationLogRepository;

@Aspect
@Component
public class LogMethodCallAspect {

    private static final String MASKED_VALUE = "***";

    private final ApplicationLogRepository applicationLogRepository;

    public LogMethodCallAspect(ApplicationLogRepository applicationLogRepository) {
        this.applicationLogRepository = applicationLogRepository;
    }

    @Around("@annotation(trd.home.common.logging.LogMethodCall)")
    public Object logMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toLongString();
        String input = formatInput(joinPoint);
        long startedAt = System.nanoTime();

        Object output;
        try {
            output = joinPoint.proceed();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            applicationLogRepository.save(ApplicationLog.successful(method, input, output, durationMs));
            return output;
        } catch (Throwable throwable) {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            applicationLogRepository.save(ApplicationLog.failed(method, input, throwable, durationMs));
            throw throwable;
        }
    }

    private String formatInput(ProceedingJoinPoint joinPoint) {
        Object[] arguments = joinPoint.getArgs();
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            var method = methodSignature.getMethod();
            if (method != null) {
                var parameters = method.getParameters();
                arguments = Arrays.copyOf(arguments, arguments.length);
                for (int index = 0; index < Math.min(parameters.length, arguments.length); index++) {
                    if (parameters[index].isAnnotationPresent(LogMasked.class)) {
                        arguments[index] = MASKED_VALUE;
                    }
                }
            }
        }
        return arguments.length == 0 ? null : Arrays.deepToString(arguments);
    }
}
