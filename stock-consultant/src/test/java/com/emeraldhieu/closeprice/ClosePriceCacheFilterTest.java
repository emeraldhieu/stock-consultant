package com.emeraldhieu.closeprice;

import static org.mockito.Mockito.*;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;

import com.emeraldhieu.cache.CacheService;
import com.emeraldhieu.cache.CachedEntity;
import com.emeraldhieu.closeprice.ClosePrice.Price;

public class ClosePriceCacheFilterTest extends CacheFilterTest {

    private ClosePriceCacheFilter closePriceCacheFilter;
    private ResourceInfo resourceInfo;
    private CacheService<Price> cacheService;
    private ContainerRequestContext containerRequestContext;
    private ContainerResponseContext containerResponseContext;
    private String ticker = "FB";
    private String startDate = "";
    private String endDate = "";

    @Before
    public void setUp() throws Exception {
        super.setUp("closeprice", ticker);
        closePriceCacheFilter = new ClosePriceCacheFilter();
        resourceInfo = mock(ResourceInfo.class);
        when(resourceInfo.getResourceMethod()).thenReturn(ClosePriceService.class.getMethod("getClosePrice", String.class, String.class, String.class));
        closePriceCacheFilter.setResourceInfo(resourceInfo);

        cacheService = new CacheService<>();
        cacheService.init();
        containerRequestContext = mock(ContainerRequestContext.class);
        containerResponseContext = mock(ContainerResponseContext.class);
        closePriceCacheFilter.setCacheService(cacheService);

        // Mock uriInfo.
        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedMap<String, String> pathParameters = new MultivaluedHashMap<>();
        pathParameters.putSingle("ticker", ticker);
        when(uriInfo.getPathParameters()).thenReturn(pathParameters);
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.putSingle("startDate", startDate);
        queryParameters.putSingle("endDate", endDate);
        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        when(containerRequestContext.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void getCachedPriceFromCache() {
        // GIVEN
        CachedEntity<Price> priceToCache = CachedEntity.<Price>builder()
                .entity(Price.builder()
                        .ticker(ticker)
                        .dateClose(dateCloseList)
                        .build())
                .build();
        cacheService.put(closePriceCacheFilter.generateHashCode(ticker, startDate, endDate), priceToCache);

        // WHEN
        closePriceCacheFilter.filter(containerRequestContext);

        // THEN
        verify(containerRequestContext, times(1)).abortWith(any());
    }
}