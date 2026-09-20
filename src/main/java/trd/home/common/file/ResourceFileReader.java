package trd.home.common.file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import trd.home.common.exception.ResourceReadException;

@Slf4j
public abstract class ResourceFileReader {

    protected List<Resource> readResources(String resourcePattern) {
        try {
            return Arrays.asList(new PathMatchingResourcePatternResolver().getResources(resourcePattern));
        } catch (Exception exception) {
            log.error("Failed to resolve resources matching pattern '{}'.", resourcePattern, exception);
            throw new ResourceReadException("Unable to read resources from pattern: " + resourcePattern, exception);
        }
    }

    protected List<String> readUtf8Lines(Resource resource) {
        try (var input = resource.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .lines()
                    .toList();
        } catch (IOException exception) {
            log.error("Failed to read resource '{}'.", resource.getFilename(), exception);
            throw new ResourceReadException("Unable to read resource: " + resource.getFilename(), exception);
        }
    }
}
