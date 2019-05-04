package com.emeraldhieu.twohundreddma;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;

import com.emeraldhieu.cache.CacheService;
import com.emeraldhieu.cache.CachedEntity;
import com.emeraldhieu.closeprice.CacheFilterTest;
import com.emeraldhieu.closeprice.ClosePrice;

public class DmaCacheFilterTest extends CacheFilterTest {
    private DmaCacheFilter dmaCacheFilter;
    private ResourceInfo resourceInfo;
    private CacheService<ClosePrice.Price> cacheService;
    private ContainerRequestContext containerRequestContext;
    private String ticker = "FB";
    private String startDate = "2012-05-18";
    private String endDate = "2012-12-04";

    @Before
    public void setUp() throws Exception {
        super.setUp("dma", ticker);
        dmaCacheFilter = new DmaCacheFilter();
        resourceInfo = mock(ResourceInfo.class);
        when(resourceInfo.getResourceMethod()).thenReturn(DmaService.class.getMethod("get200DayMovingAverage", String.class, String.class));
        dmaCacheFilter.setResourceInfo(resourceInfo);

        cacheService = new CacheService<>();
        cacheService.init();
        containerRequestContext = mock(ContainerRequestContext.class);
        dmaCacheFilter.setCacheService(cacheService);

        // Mock uriInfo.
        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedMap<String, String> pathParameters = new MultivaluedHashMap<>();
        pathParameters.putSingle("ticker", ticker);
        when(uriInfo.getPathParameters()).thenReturn(pathParameters);
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.putSingle("startDate", startDate);
        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        when(containerRequestContext.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void getCachedPriceFromCache() {
        // GIVEN
        CachedEntity<ClosePrice.Price> priceToCache = CachedEntity.<ClosePrice.Price>builder()
                .entity(ClosePrice.Price.builder()
                        .ticker(ticker)
                        .dateClose(dateCloseList)
                        .build())
                .build();
        cacheService.put(dmaCacheFilter.generateHashCode(ticker, startDate, endDate), priceToCache);

        // WHEN
        dmaCacheFilter.filter(containerRequestContext);

        // THEN
        verify(containerRequestContext, times(1)).abortWith(any());
    }
}