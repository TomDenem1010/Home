package trd.home.auth.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class ApplicationLogSearchResultTest {
    @Test
    void preservesAllLogFields() {
        var time = Instant.parse("2026-01-02T03:04:05Z");
        var result = new ApplicationLogSearchResult("method", "input", "output", "error", time, "user", 42);
        assertEquals("method", result.methodName());
        assertEquals("input", result.methodInput());
        assertEquals("output", result.methodOutput());
        assertEquals("error", result.error());
        assertEquals(time, result.createdAt());
        assertEquals("user", result.createdBy());
        assertEquals(42, result.durationMs());
    }
}
