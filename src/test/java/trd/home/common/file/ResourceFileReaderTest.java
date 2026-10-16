package trd.home.common.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import trd.home.common.exception.ResourceReadException;

class ResourceFileReaderTest {

    private final TestResourceFileReader reader = new TestResourceFileReader();

    @Test
    void wrapsResolutionFailureAndPreservesCause() throws Exception {
        var cause = new IOException("unavailable");
        try (var resolvers = mockConstruction(
                PathMatchingResourcePatternResolver.class,
                (resolver, context) ->
                        when(resolver.getResources("file:/decks/*.csv")).thenThrow(cause))) {
            var error = assertThrows(ResourceReadException.class, () -> reader.resources("file:/decks/*.csv"));
            assertSame(cause, error.getCause());
            verify(resolvers.constructed().getFirst()).getResources("file:/decks/*.csv");
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
        var resource = mock(Resource.class);
        try (var resolvers = mockConstruction(
                PathMatchingResourcePatternResolver.class,
                (resolver, context) ->
                        when(resolver.getResources("file:/decks/*.csv")).thenReturn(new Resource[] {resource}))) {
            assertEquals(List.of(resource), reader.resources("file:/decks/*.csv"));
            verify(resolvers.constructed().getFirst()).getResources("file:/decks/*.csv");
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
