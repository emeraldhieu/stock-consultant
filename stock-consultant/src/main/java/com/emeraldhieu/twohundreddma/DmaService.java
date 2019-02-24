package com.emeraldhieu.twohundreddma;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
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
import com.emeraldhieu.errorhandler.ErrorHandlingService;

import lombok.extern.apachecommons.CommonsLog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Component
@Path("api/v2")
@CommonsLog
public class DmaService {

    private static final String GET_200_DAY_MOVING_AVERAGE_URI_PATTERN =
            Config.QUANDL_API_ENDPOINT + "%s/data.json?column_index=4&collapse=daily&order=asc&limit=200&start_date=%s&api_key=" + Config.API_KEY;

    private static final String GET_FIRST_POSSIBLE_START_DATE_URI_PATTERN =
            Config.QUANDL_API_ENDPOINT + "%s/data.json?column_index=4&collapse=daily&order=asc&limit=1&api_key=" + Config.API_KEY;

    private static final String SUGGESTED_START_DATE_MESSAGE = "There is no data for this 'startDate'. Try again with 'startDate=%s'.";

    private static final String NO_DATA_FOR_ANY_DATE = "There is no data for any date.";

    private OkHttpClient client = new OkHttpClient();

    @Autowired
    private ErrorHandlingService errorHandlingService;

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

        String requestUri = String.format(GET_200_DAY_MOVING_AVERAGE_URI_PATTERN, ticker, startDate);
        Request request = new Request.Builder().url(requestUri).build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body().string();
            JSONObject responseObj = new JSONObject(responseStr);

            errorHandlingService.validateTicker(responseObj);

            JSONObject dataSetObject = (JSONObject) responseObj.get("dataset_data");
            JSONArray dataArray = (JSONArray) dataSetObject.get("data");

            suggestStartDate(ticker, startDate, dataArray);

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

    public void suggestStartDate(String ticker, String startDate, JSONArray dataArray) throws Exception {
        if (dataArray.isEmpty()) {
            String suggestedStartDate = getSuggestedStartDate(ticker, startDate);
            if (!"".equals(suggestedStartDate)) {
                throw new NotFoundException(String.format(SUGGESTED_START_DATE_MESSAGE, suggestedStartDate));
            }
            throw new NotFoundException(NO_DATA_FOR_ANY_DATE);
        }
    }

    public String getSuggestedStartDate(String ticker, String startDate) throws Exception {
        String requestUri = String.format(GET_FIRST_POSSIBLE_START_DATE_URI_PATTERN, ticker, startDate);
        Request request = new Request.Builder().url(requestUri).build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body().string();
            JSONObject responseObj = new JSONObject(responseStr);

            JSONObject dataSetObject = (JSONObject) responseObj.get("dataset_data");
            JSONArray dataArray = (JSONArray) dataSetObject.get("data");

            if (!dataArray.isEmpty()) {
                return dataArray.getJSONArray(0).getString(0);
            }
        }
        return "";
    }

    public double getAverage(JSONArray dataArray) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(dataArray.iterator(), Spliterator.ORDERED), false)
                .map(JSONArray.class::cast)
                .mapToDouble(closePrice -> closePrice.getDouble(1))
                .average().getAsDouble();
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
    public void setErrorHandlingService(ErrorHandlingService errorHandlingService) {
        this.errorHandlingService = errorHandlingService;
    }
}
