package com.emeraldhieu.twohundreddma;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import com.emeraldhieu.AbstractServiceTest;
import com.emeraldhieu.cache.CacheService;
import com.emeraldhieu.closeprice.ClosePriceService;
import com.emeraldhieu.errorhandler.ErrorHandlingService;

import okhttp3.OkHttpClient;

public class DmaServiceTest extends AbstractServiceTest {

    private DmaService dmaService;
    private CacheService cacheService;
    private ErrorHandlingService errorHandlingService;
    private ClosePriceService closePriceService;
    private String ticker = "FB";

    @Before
    public void setUp() {
        dmaService = new DmaService();
        cacheService = new DummyCacheService();
        dmaService.setCacheService(cacheService);

        errorHandlingService = new ErrorHandlingService();
        dmaService.setErrorHandlingService(errorHandlingService);

        closePriceService = new ClosePriceService();
        dmaService.setClosePriceService(closePriceService);
    }

    @Test
    public void get200dma() throws Exception {
        // GIVEN
        Path responseFilePath = Paths.get(ClassLoader.getSystemResource("responses/dma/" + ticker + ".json").toURI());
        String dmaJsonResponse = new String(Files.readAllBytes(responseFilePath));
        OkHttpClient client = initHttpClient(dmaJsonResponse);
        dmaService.setClient(client);

        // WHEN
        TwoHundredDayMovingAverage result = (TwoHundredDayMovingAverage) dmaService.get200DayMovingAverage(ticker, "2012-05-18");

        // THEN
        TwoHundredDayMovingAverage.TwoHundredDma twoHundredDma = result.getTwoHundredDma();
        assertEquals("FB", twoHundredDma.getTicker());
        assertEquals("36.1309", twoHundredDma.getAvg());
    }
}