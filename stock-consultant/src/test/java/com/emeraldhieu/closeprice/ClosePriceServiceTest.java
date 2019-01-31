package com.emeraldhieu.closeprice;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.emeraldhieu.AbstractServiceTest;
import com.emeraldhieu.cache.CacheService;
import com.emeraldhieu.errorhandler.ErrorHandlingService;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ClosePriceServiceTest extends AbstractServiceTest {

    private ClosePriceService closePriceService;
    private CacheService cacheService;
    private ErrorHandlingService errorHandlingService;
    private String ticker = "FB";

    @Before
    public void setUp() {
        cacheService = new DummyCacheService();
        errorHandlingService = new ErrorHandlingService();
        closePriceService = new ClosePriceService();
        closePriceService.setCacheService(cacheService);
        closePriceService.setErrorHandlingService(errorHandlingService);
    }

    @Test
    public void getClosePrice() throws Exception {
        // GIVEN
        String closePriceJsonReponse = "{\n" +
                "    \"dataset_data\": {\n" +
                "        \"data\": [\n" +
                "            [\n" +
                "                \"2014-12-03\",\n" +
                "                74.88\n" +
                "            ]\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        OkHttpClient client = initHttpClient(closePriceJsonReponse);
        closePriceService.setClient(client);

        // WHEN
        ClosePrice result = (ClosePrice) closePriceService.getClosePrice(ticker, "2014-01-01", "2014-12-03");

        // THEN
        List<ClosePrice.Price>
                prices = result.getPrices();
        assertEquals("FB", prices.get(0).getTicker());
        List<List<String>> dateCloseList = prices.get(0).getDateClose();
        assertEquals("2014-12-03", dateCloseList.get(0).get(0));
        assertEquals("74.88", dateCloseList.get(0).get(1));
    }
}