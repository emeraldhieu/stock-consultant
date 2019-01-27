package com.emeraldhieu.closeprice;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClosePrice {
    private final List<Price> prices;

    @Getter
    @Builder
    public static class Price {
        private final String ticker;
        private final List<List<String>> dateClose;
    }
}
