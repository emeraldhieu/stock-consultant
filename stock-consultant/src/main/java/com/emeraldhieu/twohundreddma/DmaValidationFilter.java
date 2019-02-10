package com.emeraldhieu.twohundreddma;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
public class DmaValidationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        DmaValidator dmaValidator = resourceInfo.getResourceMethod()
                .getAnnotation(DmaValidator.class);
        if (dmaValidator == null) {
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
            return;
        }
        try {
            LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new NotFoundException("Invalid date");
        }
    }
}