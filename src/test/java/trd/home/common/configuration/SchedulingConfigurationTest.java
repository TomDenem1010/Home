package trd.home.common.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SchedulingConfigurationTest {

    private final SchedulingConfiguration configuration = new SchedulingConfiguration();

    @Test
    void createsTaskSchedulerWithFourThreads() {
        var taskScheduler = configuration.taskScheduler();
        try {
            taskScheduler.initialize();

            assertEquals(4, taskScheduler.getScheduledThreadPoolExecutor().getCorePoolSize());
            assertTrue(taskScheduler.getThreadNamePrefix().startsWith("scheduler-"));
            assertEquals(true, ReflectionTestUtils.getField(taskScheduler, "waitForTasksToCompleteOnShutdown"));
        } finally {
            taskScheduler.shutdown();
        }
    }
}
