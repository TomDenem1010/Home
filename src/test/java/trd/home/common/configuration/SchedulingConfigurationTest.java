package trd.home.common.configuration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SchedulingConfigurationTest {
    @Test
    void configuresSchedulerWithoutStartingThreads() {
        var scheduler = new SchedulingConfiguration().taskScheduler();
        assertEquals(4, ReflectionTestUtils.getField(scheduler, "poolSize"));
        assertEquals("scheduler-", scheduler.getThreadNamePrefix());
        assertEquals(true, ReflectionTestUtils.getField(scheduler, "waitForTasksToCompleteOnShutdown"));
    }
}
