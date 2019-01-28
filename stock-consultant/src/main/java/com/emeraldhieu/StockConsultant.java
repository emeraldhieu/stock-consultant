package com.emeraldhieu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.emeraldhieu.closeprice.ClosePrice;
import com.emeraldhieu.closeprice.ClosePriceValidator;
import com.emeraldhieu.multidmas.MultiDma;
import com.emeraldhieu.twohundreddma.DmaValidator;
import com.emeraldhieu.twohundreddma.TwoHundredDayMovingAverage;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
@Component
@Path("api/v2")
public class StockConsultant extends Application {

    private static final String QUANDL_API_ENDPOINT = "https://www.quandl.com/api/v3/datasets/WIKI/";
    private static final String GET_CLOSE_PRICE_URI_PATTERN = QUANDL_API_ENDPOINT + "%s.json?&column_index=4&start_date=%s&end_date=%s";
    private static final String GET_200_DAY_MOVING_AVERAGE_URI_PATTERN = QUANDL_API_ENDPOINT + "%s.json?column_index=4&collapse=daily&order=asc&limit=200&start_date=%s";
    private static final String GET_FIRST_POSSIBLE_START_DATE_URI_PATTERN = QUANDL_API_ENDPOINT + "%s.json?column_index=4&collapse=daily&order=asc&limit=1";

    private static final String INVALID_TICKER_SYMBOL_ERROR_MESSAGE = "Invalid ticker symbol";

    private OkHttpClient client = new OkHttpClient();

    @GET
    @Path("")
    public String test() {
        return "HIEUGIOI";
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{tickerSymbol}/closePrice")
    @ClosePriceValidator
    public Object getClosePrice(@PathParam("tickerSymbol") String tickerSymbol,
                                @QueryParam("startDate") String startDate,
                                @QueryParam("endDate") String endDate) throws Exception {

        String requestUri = String.format(GET_CLOSE_PRICE_URI_PATTERN, tickerSymbol, startDate, endDate);
        Request request = new Request.Builder().url(requestUri).build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body().string();
            JSONObject responseObj = new JSONObject(responseStr);

            // Handle invalid ticker.
            if (!isTickerValid(responseObj)) {
                throw new NotFoundException(INVALID_TICKER_SYMBOL_ERROR_MESSAGE);
            }

            JSONObject dataSetObject = (JSONObject) responseObj.get("dataset");
            JSONArray dataArray = (JSONArray) dataSetObject.get("data");

            // Get list of dateCloses.
            Iterable<Object> iterable = () -> dataArray.iterator();
            List<List<String>> dateCloseList = StreamSupport.stream(iterable.spliterator(), false)
                    .map(JSONArray.class::cast)
                    .map(closePrice -> Arrays.asList(closePrice.getString(0), String.valueOf(closePrice.getDouble(1))))
                    .collect(Collectors.toList());

            ClosePrice.Price price = ClosePrice.Price.builder()
                    .ticker(tickerSymbol)
                    .dateClose(dateCloseList).build();

            ClosePrice closePrice = ClosePrice.builder()
                    .prices(Collections.singletonList(price))
                    .build();

            return closePrice;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{tickerSymbol}/200dma")
    @DmaValidator
    public Object get200DayMovingAverage(@PathParam("tickerSymbol") String tickerSymbol,
                                         @QueryParam("startDate") String startDate) throws Exception {

        String requestUri = startDate == null
                ? String.format(GET_FIRST_POSSIBLE_START_DATE_URI_PATTERN, tickerSymbol)
                : String.format(GET_200_DAY_MOVING_AVERAGE_URI_PATTERN, tickerSymbol, startDate);
        Request request = new Request.Builder().url(requestUri).build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body().string();
            JSONObject responseObj = new JSONObject(responseStr);

            // Handle invalid ticker.
            if (!isTickerValid(responseObj)) {
                throw new NotFoundException(INVALID_TICKER_SYMBOL_ERROR_MESSAGE);
            }

            JSONObject dataSetObject = (JSONObject) responseObj.get("dataset");
            JSONArray dataArray = (JSONArray) dataSetObject.get("data");

            if (startDate == null) {
                suggestStartDate(dataArray);
            }

            double average = StreamSupport.stream(Spliterators.spliteratorUnknownSize(dataArray.iterator(), Spliterator.ORDERED), false)
                    .map(JSONArray.class::cast)
                    .mapToDouble(closePrice -> closePrice.getDouble(1))
                    .average().getAsDouble();

            TwoHundredDayMovingAverage.TwoHundredDma twoHundredDma = TwoHundredDayMovingAverage.TwoHundredDma.builder()
                    .ticker(tickerSymbol)
                    .avg(String.valueOf(average))
                    .build();

            TwoHundredDayMovingAverage twoHundredDayMovingAverage = TwoHundredDayMovingAverage.builder()
                    .twoHundredDma(twoHundredDma).build();

            return twoHundredDayMovingAverage;
        }
    }

    private void suggestStartDate(JSONArray dataArray) {
        String errorMessage = String.format("'startDate' is missing. Try again with 'startDate=%s.'",
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(dataArray.iterator(), Spliterator.ORDERED), false)
                        .map(JSONArray.class::cast)
                        .map(closePrice -> closePrice.getString(0))
                        .findFirst().get()
        );
        throw new NotFoundException(errorMessage);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/multi")
    @DmaValidator
    public Object getMultipleDma(@QueryParam("tickers") String tickerSymbols,
                                 @QueryParam("startDate") String startDate) throws Exception {

        String[] tickers = tickerSymbols.split(",");

        List<CompletableFuture<MultiDma.TwoHundredDma.Dma>> futureList = new ArrayList<>();

        for (String ticker : tickers) {
            String requestUri = String.format(GET_200_DAY_MOVING_AVERAGE_URI_PATTERN, ticker, startDate);
            Request request = new Request.Builder().url(requestUri).build();

            CompletableFuture<MultiDma.TwoHundredDma.Dma> future = CompletableFuture.supplyAsync(() -> {
                try (Response response = client.newCall(request).execute()) {
                    String responseStr = response.body().string();
                    JSONObject responseObj = new JSONObject(responseStr);

                    // Leniently skip invalid ticker.
                    if (!isTickerValid(responseObj)) {
                        return null;
                    }

                    JSONObject dataSetObject = (JSONObject) responseObj.get("dataset");
                    JSONArray dataArray = (JSONArray) dataSetObject.get("data");

                    double average = StreamSupport.stream(Spliterators.spliteratorUnknownSize(dataArray.iterator(), Spliterator.ORDERED), false)
                            .map(JSONArray.class::cast)
                            .mapToDouble(closePrice -> closePrice.getDouble(1))
                            .average().getAsDouble();

                    MultiDma.TwoHundredDma.Dma dma = MultiDma.TwoHundredDma.Dma.builder()
                            .ticker(ticker)
                            .avg(String.valueOf(average))
                            .build();

                    return dma;
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
                return null;
            });

            futureList.add(future);
        }

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).get();

        List<MultiDma.TwoHundredDma.Dma> dmas = new ArrayList<>();
        for (CompletableFuture<MultiDma.TwoHundredDma.Dma> future : futureList) {
            MultiDma.TwoHundredDma.Dma dma = future.get();
            if (dma != null) {
                dmas.add(dma);
            }
        }

        if (dmas.isEmpty()) {
            throw new NotFoundException(INVALID_TICKER_SYMBOL_ERROR_MESSAGE);
        }

        MultiDma.TwoHundredDma twoHundredDma = MultiDma.TwoHundredDma.builder()
                .dmas(dmas)
                .build();

        MultiDma multiDma = MultiDma.builder()
                .twoHundredDma(twoHundredDma)
                .build();

        return multiDma;
    }

    private boolean isTickerValid(JSONObject responseObj) {
        if (responseObj.has("quandl_error")) {
            JSONObject errorObject = (JSONObject) responseObj.get("quandl_error");
            String errorCode = errorObject.getString("code");
            if (errorCode.equals("QECx02")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Used for testing.
     */
    public void setClient(OkHttpClient client) {
        this.client = client;
    }
}
