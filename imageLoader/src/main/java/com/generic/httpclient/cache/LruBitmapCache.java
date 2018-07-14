package com.generic.httpclient.cache;

import android.graphics.Bitmap;

import com.generic.httpclient.internal.ImageLoader;

/**
 * Created by dineshsingh on 13/03/2018.
 */
public class LruBitmapCache extends LruCache<String, Bitmap>
        implements ImageLoader.ImageCache {

    public LruBitmapCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount() / 1024;
    }

    @Override
    public Bitmap getBitmap(String key) {
        return get(key);
    }

    @Override
    public void evictBitmap(String key) {
        remove(key);
    }

    @Override
    public void evictAllBitmap() {
        evictAll();
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }

}
