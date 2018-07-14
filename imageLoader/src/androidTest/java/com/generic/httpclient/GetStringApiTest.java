package com.generic.httpclient;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.generic.httpclient.common.ILConstants;
import com.generic.httpclient.common.ILRequest;
import com.generic.httpclient.common.ILResponse;
import com.generic.httpclient.error.ILError;
import com.generic.httpclient.interfaces.StringRequestListener;

import org.junit.Rule;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static java.util.concurrent.TimeUnit.SECONDS;

public class GetStringApiTest extends ApplicationTestCase<Application> {

    @Rule
    public final MockWebServer server = new MockWebServer();

    public GetStringApiTest() {
        super(Application.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        createApplication();
    }

    public void testStringGetRequest() throws InterruptedException {

        server.enqueue(new MockResponse().setBody("data"));

        final AtomicReference<String> responseRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        HttpRequestClient.get(server.url("/").toString())
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        responseRef.set(response);
                        latch.countDown();
                    }

                    @Override
                    public void onError(ILError anError) {
                        assertTrue(false);
                    }
                });

        assertTrue(latch.await(2, SECONDS));

        assertEquals("data", responseRef.get());
    }

    public void testStringGetRequest404() throws InterruptedException {

        server.enqueue(new MockResponse().setResponseCode(404).setBody("data"));

        final AtomicReference<String> errorDetailRef = new AtomicReference<>();
        final AtomicReference<String> errorBodyRef = new AtomicReference<>();
        final AtomicReference<Integer> errorCodeRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        HttpRequestClient.get(server.url("/").toString())
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        assertTrue(false);
                    }

                    @Override
                    public void onError(ILError anError) {
                        errorBodyRef.set(anError.getErrorBody());
                        errorDetailRef.set(anError.getErrorDetail());
                        errorCodeRef.set(anError.getErrorCode());
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(2, SECONDS));

        assertEquals(ILConstants.ERROR_FROM_SERVER, errorDetailRef.get());

        assertEquals("data", errorBodyRef.get());

        assertEquals(404, errorCodeRef.get().intValue());

    }

    @SuppressWarnings("unchecked")
    public void testSynchronousStringGetRequest() throws InterruptedException {

        server.enqueue(new MockResponse().setBody("data"));

        ILRequest request = HttpRequestClient.get(server.url("/").toString()).build();

        ILResponse<String> response = request.executeForString();

        assertEquals("data", response.getResult());
    }

    @SuppressWarnings("unchecked")
    public void testSynchronousStringGetRequest404() throws InterruptedException {

        server.enqueue(new MockResponse().setResponseCode(404).setBody("data"));

        ILRequest request = HttpRequestClient.get(server.url("/").toString()).build();

        ILResponse<String> response = request.executeForString();

        ILError error = response.getError();

        assertEquals("data", error.getErrorBody());

        assertEquals(ILConstants.ERROR_FROM_SERVER, error.getErrorDetail());

        assertEquals(404, error.getErrorCode());
    }


}