package trd.home.tcg.service.file;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import trd.home.tcg.exception.UnableToReadResourcesException;

class ResourceFileReaderTest {

    @Test
    void wrapsInvalidResourcePattern() {
        TestResourceFileReader reader = new TestResourceFileReader();

        assertThrows(UnableToReadResourcesException.class, () -> reader.readResources(null));
    }

    private static class TestResourceFileReader extends ResourceFileReader {}
}
