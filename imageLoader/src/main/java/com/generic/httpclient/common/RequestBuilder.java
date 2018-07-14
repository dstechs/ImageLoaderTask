package com.generic.httpclient.common;

import java.util.concurrent.Executor;

/**
 * Created by dineshsingh on 11/07/2018.
 */
public interface RequestBuilder {

    RequestBuilder setPriority(Priority priority);

    RequestBuilder setTag(Object tag);

    RequestBuilder doNotCacheResponse();

    RequestBuilder getResponseOnlyIfCached();

    RequestBuilder getResponseOnlyFromNetwork();

    RequestBuilder setExecutor(Executor executor);

    RequestBuilder setUserAgent(String userAgent);

}
