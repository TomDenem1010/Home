package trd.home.common.configuration;

import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheManagerCustomizer<ConcurrentMapCacheManager> inMemoryCacheCustomizer() {
        return manager -> manager.setStoreByValue(true);
    }
}
