package com.emeraldhieu.twohundreddma;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TwoHundredDayMovingAverage {
    @JsonProperty("200dma")
    private TwoHundredDma twoHundredDma;

    @Getter
    @Builder
    public static class TwoHundredDma {
        private String ticker;
        private String avg;
    }
}
