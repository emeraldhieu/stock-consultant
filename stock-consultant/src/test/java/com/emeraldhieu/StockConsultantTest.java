package com.emeraldhieu;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.emeraldhieu.closeprice.ClosePrice;
import com.emeraldhieu.twohundreddma.TwoHundredDayMovingAverage;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class StockConsultantTest {

    private StockConsultant stockConsultant = new StockConsultant();
    private String tickerSymbol = "FB";

    @Before
    public void setUp() {
    }

    @Test
    public void getClosePrice() throws Exception {
        // GIVEN
        String closePriceJsonReponse = "{\n" +
                "    \"dataset\": {\n" +
                "        \"data\": [\n" +
                "            [\n" +
                "                \"2014-12-03\",\n" +
                "                74.88\n" +
                "            ]\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        initHttpClient(closePriceJsonReponse);

        // WHEN
        ClosePrice result = (ClosePrice) stockConsultant.getClosePrice(tickerSymbol, "2014-01-01", "2014-12-03");

        // THEN
        List<ClosePrice.Price>
                prices = result.getPrices();
        assertEquals("FB", prices.get(0).getTicker());
        List<List<String>> dateCloseList = prices.get(0).getDateClose();
        assertEquals("2014-12-03", dateCloseList.get(0).get(0));
        assertEquals("74.88", dateCloseList.get(0).get(1));
    }

    @Test
    public void get200dma() throws Exception {
        // GIVEN
        String dmaJsonResponse = " {\n" +
                "    \"dataset\": {\n" +
                "        \"data\": [\n" +
                "            [\n" +
                "                \"2012-05-18\",\n" +
                "                38.2318\n" +
                "            ],\n" +
                "            [\n" +
                "                \"2012-05-21\",\n" +
                "                34.03\n" +
                "            ]\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        initHttpClient(dmaJsonResponse);

        // WHEN
        TwoHundredDayMovingAverage result = (TwoHundredDayMovingAverage) stockConsultant.get200DayMovingAverage(tickerSymbol, "2012-05-18");

        // THEN
        TwoHundredDayMovingAverage.TwoHundredDma twoHundredDma = result.getTwoHundredDma();
        assertEquals("FB", twoHundredDma.getTicker());
        assertEquals("36.1309", twoHundredDma.getAvg());
    }

    private void initHttpClient(String json) throws IOException {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), json);
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://anything").build())
                .protocol(Protocol.HTTP_1_1)
                .code(201)
                .message("OK")
                .body(responseBody)
                .build();

        OkHttpClient client = mock(OkHttpClient.class);
        stockConsultant.setClient(client);
        Call call = mock(Call.class);
        when(call.execute()).thenReturn(response);
        when(client.newCall(any())).thenReturn(call);
    }
}