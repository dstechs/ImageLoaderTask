package com.generic.httpclient.internal;

/**
 * Created by dineshsingh on 12/07/2018.
 */

import android.content.Context;

import com.generic.httpclient.common.ILConstants;
import com.generic.httpclient.common.ILRequest;
import com.generic.httpclient.common.Method;
import com.generic.httpclient.error.ILError;
import com.generic.httpclient.interceptors.LogginInterceptor;
import com.generic.httpclient.utils.Utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class Networking {

    public static OkHttpClient sHttpClient = getClient();
    public static String sUserAgent = null;

    private Networking() {

    }

    public static Response performRequest(ILRequest request) throws ILError {
        Request okHttpRequest;
        Response okHttpResponse;
        try {
            Request.Builder builder = new Request.Builder().url(request.getUrl());
            switch (request.getMethod()) {
                case Method.GET: {
                    builder = builder.get();
                    break;
                }
            }
            if (request.getCacheControl() != null) {
                builder.cacheControl(request.getCacheControl());
            }
            okHttpRequest = builder.build();

            if (request.getOkHttpClient() != null) {
                request.setCall(request.getOkHttpClient().newBuilder().cache(sHttpClient.cache()).build().newCall(okHttpRequest));
            } else {
                request.setCall(sHttpClient.newCall(okHttpRequest));
            }
            okHttpResponse = request.getCall().execute();
        } catch (IOException ioe) {
            throw new ILError(ioe);
        }
        return okHttpResponse;
    }

    public static OkHttpClient getClient() {
        if (sHttpClient == null) {
            return getDefaultClient();
        }
        return sHttpClient;
    }

    public static void setClient(OkHttpClient okHttpClient) {
        sHttpClient = okHttpClient;
    }

    public static OkHttpClient getDefaultClient() {
        return new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public static void setClientWithCache(Context context) {
        sHttpClient = new OkHttpClient().newBuilder()
                .cache(Utils.getCache(context, ILConstants.MAX_CACHE_SIZE, ILConstants.CACHE_DIR_NAME))
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public static void setUserAgent(String userAgent) {
        sUserAgent = userAgent;
    }

    public static void enableLogging(LogginInterceptor.Level level) {
        LogginInterceptor logging = new LogginInterceptor();
        logging.setLevel(level);
        sHttpClient = getClient()
                .newBuilder()
                .addInterceptor(logging)
                .build();
    }

}