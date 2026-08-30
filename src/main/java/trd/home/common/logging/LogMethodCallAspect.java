package trd.home.common.logging;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import trd.home.common.dao.ApplicationLog;
import trd.home.common.repository.ApplicationLogRepository;

@Slf4j
@Aspect
@Component
public class LogMethodCallAspect {

    private static final String MASKED_VALUE = "***";

    private final ApplicationLogRepository applicationLogRepository;

    public LogMethodCallAspect(ApplicationLogRepository applicationLogRepository) {
        this.applicationLogRepository = applicationLogRepository;
    }

    @Around("@annotation(logMethodCall)")
    public Object logMethodCall(ProceedingJoinPoint joinPoint, LogMethodCall logMethodCall) throws Throwable {
        MethodCallContext context = createContext(joinPoint, logMethodCall);
        logMethodCallStarted(context);

        try {
            Object output = joinPoint.proceed();
            handleSuccessfulCall(context, output);
            return output;
        } catch (Throwable throwable) {
            handleFailedCall(context, throwable);
            throw throwable;
        }
    }

    private MethodCallContext createContext(ProceedingJoinPoint joinPoint, LogMethodCall configuration) {
        return new MethodCallContext(
                joinPoint.getSignature().toLongString(), formatInput(joinPoint), System.nanoTime(), configuration);
    }

    private void logMethodCallStarted(MethodCallContext context) {
        if (log.isDebugEnabled() && context.configuration().in()) {
            log.debug("Calling {} with input {}", context.methodName(), context.input());
        }
    }

    private void handleSuccessfulCall(MethodCallContext context, Object output) {
        logSuccessfulCall(context, output);
        auditSuccessfulCall(context, output);
    }

    private void handleFailedCall(MethodCallContext context, Throwable throwable) {
        log.error("Logged method call failed: {}", context.methodName(), throwable);
        auditFailedCall(context, throwable);
    }

    private void logSuccessfulCall(MethodCallContext context, Object output) {
        if (!log.isDebugEnabled()) {
            return;
        }

        boolean logOutput = context.configuration().out();
        boolean logDuration = context.configuration().duration();
        if (logOutput && logDuration) {
            log.debug(
                    "Completed {} with output {} in {} ms",
                    context.methodName(),
                    formatObject(output),
                    elapsedMilliseconds(context));
        } else if (logOutput) {
            log.debug("Completed {} with output {}", context.methodName(), formatObject(output));
        } else if (logDuration) {
            log.debug("Completed {} in {} ms", context.methodName(), elapsedMilliseconds(context));
        }
    }

    private void auditSuccessfulCall(MethodCallContext context, Object output) {
        if (context.configuration().audit()) {
            applicationLogRepository.save(ApplicationLog.successful(
                    context.methodName(), context.input(), output, elapsedMilliseconds(context)));
        }
    }

    private void auditFailedCall(MethodCallContext context, Throwable throwable) {
        if (context.configuration().audit()) {
            applicationLogRepository.save(ApplicationLog.failed(
                    context.methodName(), context.input(), throwable, elapsedMilliseconds(context)));
        }
    }

    private long elapsedMilliseconds(MethodCallContext context) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - context.startedAt());
    }

    private String formatInput(ProceedingJoinPoint joinPoint) {
        Object[] arguments = maskedArguments(joinPoint);
        return arguments.length == 0 ? null : Arrays.deepToString(arguments);
    }

    private Object[] maskedArguments(ProceedingJoinPoint joinPoint) {
        Object[] originalArguments = joinPoint.getArgs();
        Object[] arguments = Arrays.copyOf(originalArguments, originalArguments.length);
        if (!(joinPoint.getSignature() instanceof MethodSignature methodSignature)) {
            return arguments;
        }

        var parameters = methodSignature.getMethod().getParameters();
        for (int index = 0; index < Math.min(parameters.length, arguments.length); index++) {
            if (parameters[index].isAnnotationPresent(LogMasked.class)) {
                arguments[index] = MASKED_VALUE;
            }
        }
        return arguments;
    }

    private String formatObject(Object object) {
        if (object == null) {
            return "null";
        }
        if (!object.getClass().isArray()) {
            return object.toString();
        }

        String wrapped = Arrays.deepToString(new Object[] {object});
        return wrapped.substring(1, wrapped.length() - 1);
    }

    private record MethodCallContext(String methodName, String input, long startedAt, LogMethodCall configuration) {}
}
