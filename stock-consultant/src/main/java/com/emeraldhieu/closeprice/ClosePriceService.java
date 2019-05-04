package com.emeraldhieu.closeprice;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
import com.emeraldhieu.errorhandler.ErrorHandlingService;

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

    private OkHttpClient client = new OkHttpClient();

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
    @ClosePriceCache
    public Object getClosePrice(@PathParam("ticker") String ticker,
                                @QueryParam("startDate") String startDateParam,
                                @QueryParam("endDate") String endDateParam) throws Exception {

        String startDate = Objects.toString(startDateParam, "");
        String endDate = Objects.toString(endDateParam, "");

        String requestUri = String.format(GET_CLOSE_PRICE_URI_PATTERN, ticker, startDate, endDate);
        Request request = new Request.Builder().url(requestUri).build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body().string();
            JSONObject responseObj = new JSONObject(responseStr);

            errorHandlingService.validateTicker(responseObj);

            JSONObject dataSetObject = (JSONObject) responseObj.get("dataset_data");
            JSONArray dataArray = (JSONArray) dataSetObject.get("data");

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

            ClosePrice closePrice = ClosePrice.builder()
                    .prices(Collections.singletonList(price))
                    .build();

            return closePrice;
        }
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
    void setErrorHandlingService(ErrorHandlingService errorHandlingService) {
        this.errorHandlingService = errorHandlingService;
    }
}
