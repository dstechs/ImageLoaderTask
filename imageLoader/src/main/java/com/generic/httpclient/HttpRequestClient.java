package com.generic.httpclient;

import android.content.Context;
import android.graphics.BitmapFactory;

import com.generic.httpclient.common.ILConstants;
import com.generic.httpclient.common.ILRequest;
import com.generic.httpclient.core.Core;
import com.generic.httpclient.interceptors.LogginInterceptor;
import com.generic.httpclient.internal.ILRequestQueue;
import com.generic.httpclient.internal.ImageLoader;
import com.generic.httpclient.internal.Networking;
import com.generic.httpclient.utils.Utils;

import okhttp3.OkHttpClient;

/**
 * Created by dineshsingh on 12/07/2018.
 */

/**
 * HttpRequestClient entry point.
 * You must initialize this class before use. The simplest way is to just do
 * {#code HttpRequestClient.initialize(context)}.
 */
@SuppressWarnings("unused")
public class HttpRequestClient {

    /**
     * private constructor to prevent instantiation of this class
     */
    private HttpRequestClient() {
    }

    /**
     * Initializes HttpRequestClient with the default config.
     *
     * @param context The context
     */
    public static void initialize(Context context) {
        Networking.setClientWithCache(context.getApplicationContext());
        ILRequestQueue.initialize();
        ImageLoader.initialize();
    }

    /**
     * Initializes HttpRequestClient with the specified config.
     *
     * @param context      The context
     * @param okHttpClient The okHttpClient
     */
    public static void initialize(Context context, OkHttpClient okHttpClient) {
        if (okHttpClient != null && okHttpClient.cache() == null) {
            okHttpClient = okHttpClient
                    .newBuilder()
                    .cache(Utils.getCache(context.getApplicationContext(),
                            ILConstants.MAX_CACHE_SIZE, ILConstants.CACHE_DIR_NAME))
                    .build();
        }
        Networking.setClient(okHttpClient);
        ILRequestQueue.initialize();
        ImageLoader.initialize();
    }

    /**
     * Method to set decodeOptions
     *
     * @param decodeOptions The decode config for Bitmaps
     */
    public static void setBitmapDecodeOptions(BitmapFactory.Options decodeOptions) {
        if (decodeOptions != null) {
            ImageLoader.getInstance().setBitmapDecodeOptions(decodeOptions);
        }
    }

    /**
     * Method to make GET request
     *
     * @param url The url on which request is to be made
     * @return The GetRequestBuilder
     */
    public static ILRequest.ILRequestBuilder get(String url) {
        return new ILRequest.ILRequestBuilder(url);
    }

    /**
     * Method to cancel requests with the given tag
     *
     * @param tag The tag with which requests are to be cancelled
     */
    public static void cancel(Object tag) {
        ILRequestQueue.getInstance().cancelRequestWithGivenTag(tag, false);
    }

    /**
     * Method to force cancel requests with the given tag
     *
     * @param tag The tag with which requests are to be cancelled
     */
    public static void forceCancel(Object tag) {
        ILRequestQueue.getInstance().cancelRequestWithGivenTag(tag, true);
    }

    /**
     * Method to cancel all given request
     */
    public static void cancelAll() {
        ILRequestQueue.getInstance().cancelAll(false);
    }

    /**
     * Method to force cancel all given request
     */
    public static void forceCancelAll() {
        ILRequestQueue.getInstance().cancelAll(true);
    }

    /**
     * Method to enable logging
     */
    public static void enableLogging() {
        enableLogging(LogginInterceptor.Level.BASIC);
    }

    /**
     * Method to enable logging with tag
     *
     * @param level The level for logging
     */
    public static void enableLogging(LogginInterceptor.Level level) {
        Networking.enableLogging(level);
    }

    /**
     * Method to evict a bitmap with given key from LruCache
     *
     * @param key The key of the bitmap
     */
    public static void evictBitmap(String key) {
        final ImageLoader.ImageCache imageCache = ImageLoader.getInstance().getImageCache();
        if (imageCache != null && key != null) {
            imageCache.evictBitmap(key);
        }
    }

    /**
     * Method to clear LruCache
     */
    public static void evictAllBitmap() {
        final ImageLoader.ImageCache imageCache = ImageLoader.getInstance().getImageCache();
        if (imageCache != null) {
            imageCache.evictAllBitmap();

        }
    }

    /**
     * Method to set userAgent globally
     *
     * @param userAgent The userAgent
     */
    public static void setUserAgent(String userAgent) {
        Networking.setUserAgent(userAgent);
    }

    /**
     * Method to find if the request is running or not
     *
     * @param tag The tag with which request running status is to be checked
     * @return The request is running or not
     */
    public static boolean isRequestRunning(Object tag) {
        return ILRequestQueue.getInstance().isRequestRunning(tag);
    }

    /**
     * Shuts HttpRequestClient down
     */
    public static void shutDown() {
        Core.shutDown();
        evictAllBitmap();
    }
}
