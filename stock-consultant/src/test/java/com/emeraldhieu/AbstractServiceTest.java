package com.emeraldhieu;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.emeraldhieu.cache.CacheService;
import com.emeraldhieu.closeprice.ClosePrice;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class AbstractServiceTest {

    protected OkHttpClient initHttpClient(String json) throws IOException {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), json);
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://anything").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseBody)
                .build();

        OkHttpClient client = mock(OkHttpClient.class);
        Call call = mock(Call.class);
        when(call.execute()).thenReturn(response);
        when(client.newCall(any())).thenReturn(call);
        return client;
    }

    public class DummyCacheService extends CacheService {
        @Override
        public ClosePrice.Price get(String key) {
            return null;
        }

        @Override
        public void put(String key, ClosePrice.Price price) {
            // Do nothing.
        }
    }
}
