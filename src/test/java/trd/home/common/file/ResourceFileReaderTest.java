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
    void wrapsResolutionFailureAndPreservesCause() throws Exception {
        var cause = new java.io.IOException("unavailable");
        try (var resolvers = org.mockito.Mockito.mockConstruction(
                org.springframework.core.io.support.PathMatchingResourcePatternResolver.class,
                (resolver, context) -> org.mockito.Mockito.when(resolver.getResources("file:/decks/*.csv"))
                        .thenThrow(cause))) {
            var error = assertThrows(ResourceReadException.class, () -> reader.resources("file:/decks/*.csv"));
            org.junit.jupiter.api.Assertions.assertSame(cause, error.getCause());
        }
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

    @Test
    void delegatesPatternResolutionWithoutAccessingFilesystem() throws Exception {
        var resource = org.mockito.Mockito.mock(Resource.class);
        try (var resolvers = org.mockito.Mockito.mockConstruction(
                org.springframework.core.io.support.PathMatchingResourcePatternResolver.class,
                (resolver, context) -> org.mockito.Mockito.when(resolver.getResources("file:/decks/*.csv"))
                        .thenReturn(new Resource[] {resource}))) {
            assertEquals(List.of(resource), reader.resources("file:/decks/*.csv"));
            org.mockito.Mockito.verify(resolvers.constructed().getFirst()).getResources("file:/decks/*.csv");
        }
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
