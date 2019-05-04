package com.emeraldhieu.twohundreddma;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
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
import com.emeraldhieu.closeprice.DateCloseCacheFilter;

import lombok.extern.apachecommons.CommonsLog;

@Component
@Provider
@CommonsLog
@Priority(2000)
public class DmaCacheFilter extends DateCloseCacheFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Autowired
    private CacheService<Price> cacheService;

    private String ticker;
    private String startDate;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        DmaCache dmaCache = resourceInfo.getResourceMethod()
                .getAnnotation(DmaCache.class);
        if (dmaCache == null) {
            return;
        }

        MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();
        MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters();

        ticker = Objects.toString(pathParameters.getFirst("ticker"), "");
        startDate = Objects.toString(queryParameters.getFirst("startDate"), "");
        if (startDate.isEmpty()) {
            return;
        }

        // Get price from cache.
        LocalDate startDateObj = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDateObj = startDateObj.plusDays(200);
        CachedEntity<Price> cachedPrice = cacheService.get(generateHashCode(ticker, startDate, endDateObj.format(DateTimeFormatter.ISO_LOCAL_DATE)));

        if (cachedPrice != null) {
            log.debug("Retrieving cache of date close...");

            Price price = cachedPrice.getEntity();

            TwoHundredDayMovingAverage.TwoHundredDma twoHundredDma = TwoHundredDayMovingAverage.TwoHundredDma.builder()
                    .ticker(ticker)
                    .avg(String.valueOf(getAverage(price)))
                    .build();

            TwoHundredDayMovingAverage twoHundredDayMovingAverage = TwoHundredDayMovingAverage.builder()
                    .twoHundredDma(twoHundredDma).build();

            Response response = Response.status(Status.OK)
                    .type(requestContext.getMediaType())
                    .entity(twoHundredDayMovingAverage)
                    .build();

            requestContext.abortWith(response);
        }
    }

    public double getAverage(Price price) {
        return price.getDateClose().stream()
                .map(element -> element.get(1))
                .mapToDouble(Double::parseDouble)
                .average()
                .getAsDouble();
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