package com.emeraldhieu.closeprice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.annotation.Priority;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

import lombok.extern.apachecommons.CommonsLog;

@Component
@Provider
@CommonsLog
@Priority(1000)
public class ClosePriceValidationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        ClosePriceValidator closePriceValidator = resourceInfo.getResourceMethod()
                .getAnnotation(ClosePriceValidator.class);
        if (closePriceValidator == null) {
            return;
        }

        MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();
        // TODO Enrich validation.
        // https://dennis-xlc.gitbooks.io/restful-java-with-jax-rs-2-0-en/cn/part1/chapter12/server_side_filters.html
        validateDate(queryParameters);
    }

    private void validateDate(MultivaluedMap<String, String> queryParameters) {
        String startDate = queryParameters.getFirst("startDate");
        if (startDate == null) {
            throw new NotFoundException("Invalid date");
        }
        String endDate = queryParameters.getFirst("endDate");
        LocalDate startDateObj;
        try {
            startDateObj = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new NotFoundException("Invalid date");
        }
        if (endDate == null) {
            return;
        }
        LocalDate endDateObj;
        try {
            endDateObj = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new NotFoundException("Invalid date");
        }
        if (startDateObj != null && endDateObj != null && endDateObj.isBefore(startDateObj)) {
            throw new NotFoundException("Invalid date range");
        }
    }
}