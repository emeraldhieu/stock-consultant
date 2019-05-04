package com.emeraldhieu.closeprice;

import java.nio.charset.Charset;

import javax.ws.rs.container.ContainerRequestFilter;

import com.google.common.hash.Hashing;

public abstract class DateCloseCacheFilter implements ContainerRequestFilter {

    public String generateHashCode(String ticker, String startDate, String endDate) {
        String hashCode = Hashing.sha256()
                .hashString(ticker + startDate + endDate,
                        Charset.defaultCharset())
                .toString();
        return hashCode;
    }
}
