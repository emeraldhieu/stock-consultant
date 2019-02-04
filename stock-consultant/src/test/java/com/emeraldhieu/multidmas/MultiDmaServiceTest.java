package com.emeraldhieu.multidmas;

import static com.emeraldhieu.multidmas.MultiDmaService.GET_200_DAY_MOVING_AVERAGE_URI_PATTERN;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.emeraldhieu.AbstractServiceTest;
import com.emeraldhieu.cache.CacheService;
import com.emeraldhieu.errorhandler.ErrorHandlingService;
import com.emeraldhieu.twohundreddma.DmaService;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MultiDmaServiceTest extends AbstractServiceTest {
    private DmaService dmaService;
    private CacheService cacheService;
    private ErrorHandlingService errorHandlingService;
    private MultiDmaService multiDmaService;

    @Before
    public void setUp() {
        multiDmaService = new MultiDmaService();

        cacheService = new DummyCacheService();
        multiDmaService.setCacheService(cacheService);

        errorHandlingService = new ErrorHandlingService();
        multiDmaService.setErrorHandlingService(errorHandlingService);

        dmaService = new DmaService();
        multiDmaService.setDmaService(dmaService);
    }

    @Test
    public void getMultiDmas() throws Exception {
        // GIVEN
        OkHttpClient client = mock(OkHttpClient.class);
        Call fbCall = mockCall("FB");
        Call msftCall = mockCall("MSFT");
        Call twtrCall = mockCall("TWTR");
        Map<String, Call> calls = new HashMap<>();
        calls.put("FB", fbCall);
        calls.put("MSFT", msftCall);
        calls.put("TWTR", twtrCall);

        when(client.newCall(any())).thenAnswer(new MyAnswer(calls));

        when(client.newCall(Mockito.argThat(new RequestByTickerMatcher("FB")))).thenReturn(fbCall);
        when(client.newCall(Mockito.argThat(new RequestByTickerMatcher("MSFT")))).thenReturn(msftCall);
        when(client.newCall(Mockito.argThat(new RequestByTickerMatcher("TWTR")))).thenReturn(twtrCall);

        multiDmaService.setClient(client);

        // WHEN
        MultiDma result = (MultiDma) multiDmaService.getMultipleDma("FB,MSFT,TWTR", "2000-01-01");

        // THEN
        MultiDma.TwoHundredDma twoHundredDma = result.getTwoHundredDma();
        List<MultiDma.TwoHundredDma.Dma> twoHundredDmas = twoHundredDma.getDmas();

        assertDma(twoHundredDmas, "FB", "34.4206");
        assertDma(twoHundredDmas, "MSFT", "114.33");
        assertDma(twoHundredDmas, "TWTR", "43.15");
    }

    private Call mockCall(String ticker) throws Exception {
        String multidmasReponse = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("responses/multidma/" + ticker + ".json").toURI())));

        String requestUri = String.format(GET_200_DAY_MOVING_AVERAGE_URI_PATTERN, ticker, "2000-01-01");
        Request request = new Request.Builder()
                .url(requestUri)
                .build();

        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), multidmasReponse);
        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseBody)
                .build();

        Call call = mock(Call.class);
        when(call.execute()).thenReturn(response);
        return call;
    }

    private class MyAnswer implements Answer<Call> {
        private final Map<String, Call> calls;

        public MyAnswer(Map<String, Call> calls) {
            this.calls = calls;
        }

        @Override
        public Call answer(InvocationOnMock invocationOnMock) {
            Object[] args = invocationOnMock.getArguments();
            Request requestCreatedInEndpoint = (Request) args[0];
            if (requestCreatedInEndpoint == null) {
                return null;
            }
            String uri = requestCreatedInEndpoint.url().toString();
            String ticker = StringUtils.substringBetween(uri, "WIKI/", "/data");
            Call call = calls.get(ticker);
            return call;
        }
    }

    public class RequestByTickerMatcher extends ArgumentMatcher<Request> {
        private final String ticker;

        public RequestByTickerMatcher(String ticker) {
            this.ticker = ticker;
        }

        @Override
        public boolean matches(Object arg) {
            Request request = (Request) arg;
            if (request == null) {
                return false;
            }
            return request.url().toString().contains(ticker);
        }
    }

    private void assertDma(List<MultiDma.TwoHundredDma.Dma> twoHundreadDmas, String ticker, String average) {
        assertTrue(twoHundreadDmas.stream()
                .anyMatch(dma -> dma.getTicker().equals(ticker)
                        && dma.getAvg().equals(average)));
    }
}