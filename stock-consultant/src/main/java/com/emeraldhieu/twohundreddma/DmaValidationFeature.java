package com.emeraldhieu.twohundreddma;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

@Component
@Provider
public class DmaValidationFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        DmaValidator dmaValidator = resourceInfo.getResourceMethod()
                .getAnnotation(DmaValidator.class);
        if (dmaValidator != null) {
            context.register(DmaValidationFilter.class);
        }
    }
}
