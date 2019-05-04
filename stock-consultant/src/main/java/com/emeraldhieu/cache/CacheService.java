package com.emeraldhieu.cache;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.stereotype.Component;

@Component
public class CacheService<T> {
    /**
     * Store 10000 frequently used elements.
     */
    private static int MAX_ENTRY_COUNT = 10000;

    private CacheManager cacheManager;

    public static final String DEFAULT_CACHE_KEY = "default";

    private Cache<String, CachedEntity> cache;

    @PostConstruct
    public void init() {
         cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(DEFAULT_CACHE_KEY,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, CachedEntity.class,
                                ResourcePoolsBuilder.heap(MAX_ENTRY_COUNT)))
                 .build();
        cacheManager.init();
        cache = cacheManager.getCache(DEFAULT_CACHE_KEY, String.class, CachedEntity.class);
    }

    public CachedEntity<T> get(String key) {
        return cache.get(key);
    }

    public void put(String key, CachedEntity<T> value) {
        cache.put(key, value);
    }

    @PreDestroy
    public void cleanUp() {
        cacheManager.close();
    }
}
