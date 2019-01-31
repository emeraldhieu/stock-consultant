package com.emeraldhieu.twohundreddma;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
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
import com.emeraldhieu.closeprice.ClosePrice;
import com.emeraldhieu.closeprice.ClosePriceService;
import com.emeraldhieu.errorhandler.ErrorHandlingService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
@Path("api/v2")
public class DmaService {

    private static final String GET_200_DAY_MOVING_AVERAGE_URI_PATTERN =
            Config.QUANDL_API_ENDPOINT + "%s/data.json?column_index=4&collapse=daily&order=asc&limit=200&start_date=%s&api_key=" + Config.API_KEY;

    private static final String GET_FIRST_POSSIBLE_START_DATE_URI_PATTERN =
            Config.QUANDL_API_ENDPOINT + "%s/data.json?column_index=4&collapse=daily&order=asc&limit=1&api_key=" + Config.API_KEY;

    private OkHttpClient client = new OkHttpClient();

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ErrorHandlingService errorHandlingService;

    @Autowired
    private ClosePriceService closePriceService;

    @PostConstruct
    public void init() {

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{ticker}/200dma")
    @DmaValidator
    public Object get200DayMovingAverage(@PathParam("ticker") String ticker,
                                         @QueryParam("startDate") String startDateParam) throws Exception {
        String startDate = Objects.toString(startDateParam, "");

        LocalDate startDateObj = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDateObj = startDateObj.plusDays(200);

        // Get price from cache.
        ClosePrice.Price cachedPrice = cacheService.get(closePriceService
                .generateHashCode(ticker, startDate, endDateObj.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        if (cachedPrice != null) {
            TwoHundredDayMovingAverage.TwoHundredDma twoHundredDma = TwoHundredDayMovingAverage.TwoHundredDma.builder()
                    .ticker(ticker)
                    .avg(String.valueOf(getAverage(cachedPrice)))
                    .build();
            TwoHundredDayMovingAverage twoHundredDayMovingAverage = TwoHundredDayMovingAverage.builder()
                    .twoHundredDma(twoHundredDma).build();
            return  twoHundredDayMovingAverage;
        }

        String requestUri = "".equals(startDate)
                ? String.format(GET_FIRST_POSSIBLE_START_DATE_URI_PATTERN, ticker)
                : String.format(GET_200_DAY_MOVING_AVERAGE_URI_PATTERN, ticker, startDate);
        Request request = new Request.Builder().url(requestUri).build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body().string();
            JSONObject responseObj = new JSONObject(responseStr);

            errorHandlingService.validateTicker(responseObj);

            JSONObject dataSetObject = (JSONObject) responseObj.get("dataset_data");
            JSONArray dataArray = (JSONArray) dataSetObject.get("data");

            closePriceService.suggestStartDate(startDate, dataArray);

            // TODO Extract Price from dataArray and cache it.

            TwoHundredDayMovingAverage.TwoHundredDma twoHundredDma = TwoHundredDayMovingAverage.TwoHundredDma.builder()
                    .ticker(ticker)
                    .avg(String.valueOf(getAverage(dataArray)))
                    .build();

            TwoHundredDayMovingAverage twoHundredDayMovingAverage = TwoHundredDayMovingAverage.builder()
                    .twoHundredDma(twoHundredDma).build();

            return twoHundredDayMovingAverage;
        }
    }

    public double getAverage(JSONArray dataArray) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(dataArray.iterator(), Spliterator.ORDERED), false)
                .map(JSONArray.class::cast)
                .mapToDouble(closePrice -> closePrice.getDouble(1))
                .average().getAsDouble();
    }

    private double getAverage(ClosePrice.Price price) {
        return price.getDateClose().stream()
                .map(dateClose -> dateClose.get(1))
                .mapToDouble(Double::parseDouble)
                .average()
                .getAsDouble();
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
    public void setErrorHandlingService(ErrorHandlingService errorHandlingService) {
        this.errorHandlingService = errorHandlingService;
    }

    /**
     * Used for testing.
     */
    public void setClosePriceService(ClosePriceService closePriceService) {
        this.closePriceService = closePriceService;
    }
}
