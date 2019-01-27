package com.emeraldhieu.closeprice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.emeraldhieu.twohundreddma.DmaValidator;

@Provider
@ClosePriceValidator
@PreMatching
public class ClosePriceValidationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();
        // TODO Enrich validation.
        // https://dennis-xlc.gitbooks.io/restful-java-with-jax-rs-2-0-en/cn/part1/chapter12/server_side_filters.html
        validateDate(queryParameters);
    }

    private void validateDate(MultivaluedMap<String, String> queryParameters) {
        String startDate = queryParameters.getFirst("startDate");
        if (startDate == null) {
            // TODO
            return;
        }
        String endDate = queryParameters.getFirst("endDate");
        LocalDate startDateObj = null, endDateObj = null;
        if (startDate != null) {
            try {
                startDateObj = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                throw new NotFoundException("Invalid date");
            }
        }
        if (endDate != null) {
            try {
                endDateObj = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                throw new NotFoundException("Invalid date");
            }
        }
        if (startDateObj != null && endDateObj != null && endDateObj.isBefore(startDateObj)) {
            throw new NotFoundException("Invalid date range");
        }
    }
}