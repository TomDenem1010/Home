package trd.home.tcg.service.file;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import trd.home.tcg.exception.UnableToReadResourcesException;

public abstract class ResourceFileReader {

    Resource[] readResources(String resourcePattern) {
        try {
            return new PathMatchingResourcePatternResolver().getResources(resourcePattern);
        } catch (Exception exception) {
            throw new UnableToReadResourcesException(
                    "Unable to read resources from pattern: " + resourcePattern, exception);
        }
    }
}
