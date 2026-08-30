package trd.home.tcg.service.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import trd.home.tcg.exception.UnableToReadResourcesException;

@Slf4j
public abstract class ResourceFileReader {

    Resource[] readResources(String resourcePattern) {
        try {
            return new PathMatchingResourcePatternResolver().getResources(resourcePattern);
        } catch (Exception exception) {
            log.error("Failed to resolve resources matching pattern '{}'", resourcePattern, exception);
            throw new UnableToReadResourcesException(
                    "Unable to read resources from pattern: " + resourcePattern, exception);
        }
    }
}
