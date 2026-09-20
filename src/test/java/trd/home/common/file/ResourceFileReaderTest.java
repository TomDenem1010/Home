package trd.home.common.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import trd.home.common.exception.ResourceReadException;

class ResourceFileReaderTest {

    private final TestResourceFileReader reader = new TestResourceFileReader();

    @Test
    void wrapsInvalidResourcePattern() {
        assertThrows(ResourceReadException.class, () -> reader.resources(null));
    }

    @Test
    void readsUtf8ResourceAsLines() {
        Resource resource =
                new InputStreamResource(new ByteArrayInputStream("first\nsecond".getBytes(StandardCharsets.UTF_8))) {
                    @Override
                    public String getFilename() {
                        return "values.txt";
                    }
                };

        assertEquals(List.of("first", "second"), reader.lines(resource));
    }

    private static class TestResourceFileReader extends ResourceFileReader {

        private List<Resource> resources(String pattern) {
            return readResources(pattern);
        }

        private List<String> lines(Resource resource) {
            return readUtf8Lines(resource);
        }
    }
}
