package trd.home.common.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SchedulingConfigurationTest {

    private final SchedulingConfiguration configuration = new SchedulingConfiguration();

    @Test
    void createsTaskSchedulerWithFourThreads() {
        var taskScheduler = configuration.taskScheduler();
        try {
            taskScheduler.initialize();

            assertEquals(4, taskScheduler.getScheduledThreadPoolExecutor().getCorePoolSize());
            assertTrue(taskScheduler.getThreadNamePrefix().startsWith("scheduler-"));
        } finally {
            taskScheduler.shutdown();
        }
    }
}
