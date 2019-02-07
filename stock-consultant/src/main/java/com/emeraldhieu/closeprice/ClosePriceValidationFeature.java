package com.emeraldhieu.closeprice;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

@Component
@Provider
public class ClosePriceValidationFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        ClosePriceValidator closePriceValidator = resourceInfo.getResourceMethod()
                .getAnnotation(ClosePriceValidator.class);
        if (closePriceValidator != null) {
            context.register(ClosePriceValidationFilter.class);
        }
    }
}
