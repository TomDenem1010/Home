package trd.home.common.dao;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ApplicationLogTest {
    @Test
    void supportsJpaConstructionAndGeneratedIdentity() {
        var log = new ApplicationLog();
        assertNull(log.getId());
        assertNull(log.getMethod());
        assertNull(log.getInput());
        assertNull(log.getOutput());
        assertNull(log.getError());
        assertEquals(0, log.getDurationMs());
    }
}
