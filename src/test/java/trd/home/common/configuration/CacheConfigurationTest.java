package trd.home.common.configuration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class CacheConfigurationTest {
    @Test
    void cachesIndependentCopiesOfMutableValues() {
        var manager = new ConcurrentMapCacheManager("test");
        manager.setBeanClassLoader(getClass().getClassLoader());
        new CacheConfiguration().inMemoryCacheCustomizer().customize(manager);
        var cache = manager.getCache("test");
        var original = new ArrayList<>(List.of("original"));
        cache.put("key", original);
        original.add("changed");
        var retrieved = cache.get("key", ArrayList.class);
        assertEquals(List.of("original"), retrieved);
        retrieved.clear();
        assertEquals(List.of("original"), cache.get("key").get());
    }
}
