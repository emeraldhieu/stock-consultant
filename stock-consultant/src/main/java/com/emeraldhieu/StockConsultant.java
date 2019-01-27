package com.emeraldhieu;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.constraints.NotNull;
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

import com.emeraldhieu.converter.DateFormat;
import com.emeraldhieu.validator.QueryParamValidator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Component
@Path("api/v2")
public class StockConsultant extends Application {

    private static final String GET_CLOSE_PRICE_URI_PATTERN = "https://www.quandl.com/api/v3/datasets/WIKI/%s.json?&column_index=4&start_date=%s&end_date=%s";

    private OkHttpClient client = new OkHttpClient();

    @GET
    @Path("")
    public String test() {
        return "HIEUGIOI";
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{tickerSymbol}/closePrice")
    @QueryParamValidator
    public Object getClosePrice(@NotNull @PathParam("tickerSymbol") String tickerSymbol,
                                @NotNull @QueryParam("startDate") @DateFormat Date startDate,
                                @NotNull @QueryParam("endDate") @DateFormat Date endDate) throws Exception {

        // Handle invalid range of dates.
        if (endDate.before(startDate)) {
            throw new NotFoundException("Invalid range of dates");
        }

        LocalDateTime start = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        String startDateStr = DateTimeFormatter.ISO_LOCAL_DATE.format(start);

        LocalDateTime end = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());
        String endDateStr = DateTimeFormatter.ISO_LOCAL_DATE.format(end);

        String requestUri = String.format(GET_CLOSE_PRICE_URI_PATTERN, tickerSymbol, startDateStr, endDateStr);
        Request request = new Request.Builder().url(requestUri).build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            String responseStr = body.string();
            JSONObject outerObject = new JSONObject(responseStr);

            // Handle invalid ticker.
            if (outerObject.has("quandl_error")) {
                JSONObject errorObject = (JSONObject) outerObject.get("quandl_error");
                String errorCode = errorObject.getString("code");
                if (errorCode.equals("QECx02")) {
                    throw new NotFoundException("Invalid ticker symbol");
                }
            }

            JSONObject dataSetObject = (JSONObject) outerObject.get("dataset");
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
}
