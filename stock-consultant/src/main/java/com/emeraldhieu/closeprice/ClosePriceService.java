package com.emeraldhieu.closeprice;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.emeraldhieu.Config;
import com.emeraldhieu.cache.CacheService;
import com.emeraldhieu.errorhandler.ErrorHandlingService;
import com.google.common.hash.Hashing;

import lombok.extern.apachecommons.CommonsLog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
@Path("api/v2")
@CommonsLog
public class ClosePriceService {

    private static final String GET_CLOSE_PRICE_URI_PATTERN =
            Config.QUANDL_API_ENDPOINT + "%s/data.json?order=asc&column_index=4&start_date=%s&end_date=%s&api_key=" + Config.API_KEY;

    private static final String SUGGESTED_START_DATE_MESSAGE = "'startDate' is missing. Try again with 'startDate=%s'.";

    private OkHttpClient client = new OkHttpClient();

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ErrorHandlingService errorHandlingService;

    @PostConstruct
    public void init() {

    }

    @GET
    @Path("hello")
    public Object hello() {
        return "Hello world";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{ticker}/closePrice")
    @ClosePriceValidator
    public Object getClosePrice(@PathParam("ticker") String ticker,
                                @QueryParam("startDate") String startDateParam,
                                @QueryParam("endDate") String endDateParam) throws Exception {

        String startDate = Objects.toString(startDateParam, "");
        String endDate = Objects.toString(endDateParam, "");

        // Get price from cache.
        ClosePrice.Price cachedPrice = cacheService.get(generateHashCode(ticker, startDate, endDate));

        // Return cached close price built from price.
        if (cachedPrice != null) {
            log.debug("Retrieving cache of close price...");
            ClosePrice closePrice = ClosePrice.builder()
                    .prices(Collections.singletonList(cachedPrice))
                    .build();
            return closePrice;
        }

        String requestUri = String.format(GET_CLOSE_PRICE_URI_PATTERN, ticker, startDate, endDate);
        Request request = new Request.Builder().url(requestUri).build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body().string();
            JSONObject responseObj = new JSONObject(responseStr);

            errorHandlingService.validateTicker(responseObj);

            JSONObject dataSetObject = (JSONObject) responseObj.get("dataset_data");
            JSONArray dataArray = (JSONArray) dataSetObject.get("data");

            suggestStartDate(startDate, dataArray);

            // Get list of dateCloses.
            Iterable<Object> iterable = () -> dataArray.iterator();
            List<List<String>> dateCloseList = StreamSupport.stream(iterable.spliterator(), false)
                    .map(JSONArray.class::cast)
                    .map(closePrice -> Arrays.asList(closePrice.getString(0), String.valueOf(closePrice.getDouble(1))))
                    .collect(Collectors.toList());

            ClosePrice.Price price = ClosePrice.Price.builder()
                    .ticker(ticker)
                    .dateClose(dateCloseList)
                    .build();

            // Cache the price!
            log.debug("Caching close price...");
            cacheService.put(generateHashCode(ticker, startDate, endDate), price);

            ClosePrice closePrice = ClosePrice.builder()
                    .prices(Collections.singletonList(price))
                    .build();

            return closePrice;
        }
    }

    public void suggestStartDate(String startDate, JSONArray dataArray) {
        if ("".equals(startDate)) {
            throw new NotFoundException(String.format(SUGGESTED_START_DATE_MESSAGE, getSuggestedStartDate(dataArray)));
        }
    }

    public String getSuggestedStartDate(JSONArray dataArray) {
        String suggestedStartDate = StreamSupport.stream(Spliterators.spliteratorUnknownSize(dataArray.iterator(), Spliterator.ORDERED), false)
                .map(JSONArray.class::cast)
                .map(closePrice -> closePrice.getString(0))
                .findFirst().get();
        return suggestedStartDate;
    }

    public String generateHashCode(String ticker, String startDate, String endDate) {
        String hashCode = Hashing.sha256()
                .hashString(ticker + startDate + endDate,
                        Charset.defaultCharset())
                .toString();
        return hashCode;
    }

    /**
     * Used for testing.
     */
    void setClient(OkHttpClient client) {
        this.client = client;
    }

    /**
     * Used for testing.
     */
    void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Used for testing.
     */
    void setErrorHandlingService(ErrorHandlingService errorHandlingService) {
        this.errorHandlingService = errorHandlingService;
    }
}
