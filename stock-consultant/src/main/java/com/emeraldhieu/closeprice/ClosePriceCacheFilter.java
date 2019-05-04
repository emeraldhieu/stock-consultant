package com.emeraldhieu.closeprice;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.emeraldhieu.cache.CacheService;
import com.emeraldhieu.cache.CachedEntity;
import com.emeraldhieu.closeprice.ClosePrice.Price;

import lombok.extern.apachecommons.CommonsLog;

@Component
@Provider
@CommonsLog
@Priority(2000)
public class ClosePriceCacheFilter extends DateCloseCacheFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Autowired
    private CacheService<Price> cacheService;

    private String ticker;
    private String startDate;
    private String endDate;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        ClosePriceCache closePriceCache = resourceInfo.getResourceMethod()
                .getAnnotation(ClosePriceCache.class);
        if (closePriceCache == null) {
            return;
        }

        MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();
        MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters();

        ticker = Objects.toString(pathParameters.getFirst("ticker"), "");
        startDate = Objects.toString(queryParameters.getFirst("startDate"), "");
        endDate = Objects.toString(queryParameters.getFirst("endDate"), "");

        // Get price from cache.
        CachedEntity<Price> cachedPrice = cacheService.get(generateHashCode(ticker, startDate, endDate));

        if (cachedPrice != null) {
            log.debug("Retrieving cache of date close...");
            Response response = Response.status(Status.OK)
                    .type(requestContext.getMediaType())
                    .entity(cachedPrice)
                    .build();

            requestContext.abortWith(response);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        ClosePriceCache closePriceCache = resourceInfo.getResourceMethod()
                .getAnnotation(ClosePriceCache.class);
        if (closePriceCache == null) {
            return;
        }

        if (responseContext.getStatusInfo() != Status.OK // If error occurs
                || responseContext.getEntity() instanceof CachedEntity) { // or if a cache is being retrieved
            return;
        }

        log.debug("Caching close price...");

        ClosePrice closePrice = (ClosePrice) responseContext.getEntity();
        Price price = closePrice.getPrices().get(0);
        CachedEntity<Price> cachedPrice = CachedEntity.<Price>builder().entity(price).build();

        // Cache the price!
        cacheService.put(generateHashCode(ticker, startDate, endDate), cachedPrice);
    }

    /**
     * Used for testing.
     */
    void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Used for testing.
     */
    void setResourceInfo(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }
}