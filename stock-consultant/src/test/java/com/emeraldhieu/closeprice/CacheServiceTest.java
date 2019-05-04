package com.emeraldhieu.closeprice;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.emeraldhieu.cache.CacheService;
import com.emeraldhieu.cache.CachedEntity;
import com.emeraldhieu.closeprice.ClosePrice.Price;

public class CacheServiceTest {

    private CacheService cacheService;
    private String ticker = "FB";

    @Before
    public void setUp() {
        cacheService = new CacheService();
        cacheService.init();
    }

    @Test
    public void caching() {
        // GIVEN
        CachedEntity<Price> priceToCache = CachedEntity.<Price>builder()
                .entity(Price.builder()
                        .ticker(ticker)
                        .build())
                .build();

        // WHEN
        cacheService.put(CacheService.DEFAULT_CACHE_KEY, priceToCache);

        // THEN
        CachedEntity<Price> cachedPrice = cacheService.get(CacheService.DEFAULT_CACHE_KEY);
        Price price = cachedPrice.getEntity();
        assertEquals(ticker, price.getTicker());
    }
}