package trd.home.common.configuration;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class CacheConfigurationTest {
    @Test
    void configuresCacheToStoreValuesByCopy() {
        var manager = mock(ConcurrentMapCacheManager.class);
        new CacheConfiguration().inMemoryCacheCustomizer().customize(manager);
        verify(manager).setStoreByValue(true);
    }
}
