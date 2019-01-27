package com.emeraldhieu.validator;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

@Provider
@QueryParamValidator
@PreMatching
public class ValidationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        MultivaluedMap< String, String > queryParameters = requestContext.getUriInfo().getQueryParameters();
        // TODO Implement validation right here instead of in resource method.
        // https://dzone.com/articles/validating-jax-rs-query-parameters
        // https://dennis-xlc.gitbooks.io/restful-java-with-jax-rs-2-0-en/cn/part1/chapter12/server_side_filters.html
    }
}