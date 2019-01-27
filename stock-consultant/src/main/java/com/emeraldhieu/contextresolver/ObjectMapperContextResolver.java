package com.emeraldhieu.contextresolver;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * ContextResolver that automatically configures and provides {@link ObjectMapper} used by Jackson.
 */
@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}