package com.emeraldhieu.multidmas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
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
import com.emeraldhieu.twohundreddma.DmaService;
import com.emeraldhieu.twohundreddma.DmaValidator;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
@Component
@Path("api/v2")
public class MultiDmaService {

    static final String GET_200_DAY_MOVING_AVERAGE_URI_PATTERN =
            Config.QUANDL_API_ENDPOINT + "%s/data.json?column_index=4&collapse=daily&order=asc&limit=200&start_date=%s&api_key=" + Config.API_KEY;

    private OkHttpClient client = new OkHttpClient();

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ErrorHandlingService errorHandlingService;

    @Autowired
    private DmaService dmaService;

    @PostConstruct
    public void init() {

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/multi")
    @DmaValidator
    public Object getMultipleDma(@QueryParam("tickers") String tickersParam,
                                 @QueryParam("startDate") String startDateParam) throws Exception {

        String startDate = Objects.toString(startDateParam, "");

        String[] tickers = tickersParam.split(",");

        List<CompletableFuture<MultiDma.TwoHundredDma.Dma>> futureList = new ArrayList<>();

        for (String ticker : tickers) {
            String requestUri = String.format(GET_200_DAY_MOVING_AVERAGE_URI_PATTERN, ticker, startDate);
            Request request = new Request.Builder().url(requestUri).build();

            CompletableFuture<MultiDma.TwoHundredDma.Dma> future = CompletableFuture.supplyAsync(() -> {
                try (Response response = client.newCall(request).execute()) {
                    String responseStr = response.body().string();
                    JSONObject responseObj = new JSONObject(responseStr);

                    // Leniently skip invalid ticker.
                    if (!errorHandlingService.isTickerValid(responseObj)) {
                        MultiDma.TwoHundredDma.Dma invalidTickerDma = MultiDma.TwoHundredDma.Dma.builder()
                                .ticker(ticker)
                                .errorMessage(errorHandlingService.getInvalidTickerSymbolErrorMessage())
                                .build();
                        return invalidTickerDma;
                    }

                    if (!errorHandlingService.isRequestSlowEnough(responseObj)) {
                        MultiDma.TwoHundredDma.Dma invalidTickerDma = MultiDma.TwoHundredDma.Dma.builder()
                                .ticker(ticker)
                                .errorMessage(errorHandlingService.getTooFastRequestErrorMessage())
                                .build();
                        return invalidTickerDma;
                    }

                    JSONObject dataSetObject = (JSONObject) responseObj.get("dataset_data");
                    JSONArray dataArray = (JSONArray) dataSetObject.get("data");

                    MultiDma.TwoHundredDma.Dma dma = MultiDma.TwoHundredDma.Dma.builder()
                            .ticker(ticker)
                            .avg(String.valueOf(dmaService.getAverage(dataArray)))
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
            throw new NotFoundException(errorHandlingService.getInvalidTickerSymbolErrorMessage());
        }

        MultiDma.TwoHundredDma twoHundredDma = MultiDma.TwoHundredDma.builder()
                .dmas(dmas)
                .build();

        MultiDma multiDma = MultiDma.builder()
                .twoHundredDma(twoHundredDma)
                .build();

        return multiDma;
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

    /**
     * Used for testing.
     */
    void setClient(OkHttpClient client) {
        this.client = client;
    }

    /**
     * Used for testing.
     */
    void setDmaService(DmaService dmaService) {
        this.dmaService = dmaService;
    }
}
