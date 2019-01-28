package com.emeraldhieu.multidmas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MultiDma {

    @JsonProperty("200dma")
    private TwoHundredDma twoHundredDma;

    @Builder
    @Getter
    public static class TwoHundredDma {
        private List<Dma> dmas;

        @Builder
        @Getter
        public static class Dma {
            private String ticker;
            private String avg;
        }

    }
}
