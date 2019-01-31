package com.emeraldhieu.cache;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.stereotype.Component;

import com.emeraldhieu.closeprice.ClosePrice;

@Component
public class CacheService {
    /**
     * Store 10000 frequently used elements.
     */
    private static int MAX_ENTRY_COUNT = 10000;

    private CacheManager cacheManager;

    /**
     * Map of hashCode and dateClose. To get a dateClose, pass hashCode.
     */
    private Cache<String, ClosePrice.Price> dateCloseCache;

    @PostConstruct
    public void init() {
         cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("dateClose",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, ClosePrice.Price.class,
                                ResourcePoolsBuilder.heap(MAX_ENTRY_COUNT)))
                 .build();
        cacheManager.init();
        dateCloseCache = cacheManager.getCache("dateClose", String.class, ClosePrice.Price.class);
    }

    public ClosePrice.Price get(String key) {
        return dateCloseCache.get(key);
    }

    public void put(String key, ClosePrice.Price price) {
        dateCloseCache.put(key, price);
    }

    @PreDestroy
    public void cleanUp() {
        cacheManager.close();
    }
}
