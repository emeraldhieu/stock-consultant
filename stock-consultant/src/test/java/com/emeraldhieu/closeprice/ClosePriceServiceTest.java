package com.emeraldhieu.closeprice;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.emeraldhieu.AbstractServiceTest;
import com.emeraldhieu.errorhandler.ErrorHandlingService;

import okhttp3.OkHttpClient;

public class ClosePriceServiceTest extends AbstractServiceTest {

    private ClosePriceService closePriceService;
    private ErrorHandlingService errorHandlingService;
    private String ticker = "FB";

    @Before
    public void setUp() {
        errorHandlingService = new ErrorHandlingService();
        closePriceService = new ClosePriceService();
        closePriceService.setErrorHandlingService(errorHandlingService);
    }

    @Test
    public void getClosePrice() throws Exception {
        // GIVEN
        Path responseFilePath = Paths.get(ClassLoader.getSystemResource("responses/closeprice/" + ticker + ".json").toURI());
        String closePriceJsonReponse = new String(Files.readAllBytes(responseFilePath));
        OkHttpClient client = initHttpClient(closePriceJsonReponse);
        closePriceService.setClient(client);

        // WHEN
        ClosePrice result = (ClosePrice) closePriceService.getClosePrice(ticker, "2014-01-01", "2014-12-03");

        // THEN
        List<ClosePrice.Price> prices = result.getPrices();
        assertEquals("FB", prices.get(0).getTicker());
        List<List<String>> dateCloseList = prices.get(0).getDateClose();
        assertEquals("2014-12-02", dateCloseList.get(0).get(0));
        assertEquals("75.46", dateCloseList.get(0).get(1));
        assertEquals("2014-12-03", dateCloseList.get(1).get(0));
        assertEquals("74.88", dateCloseList.get(1).get(1));
    }
}