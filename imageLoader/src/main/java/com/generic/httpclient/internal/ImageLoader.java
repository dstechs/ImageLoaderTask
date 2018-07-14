package com.generic.httpclient.internal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.generic.httpclient.HttpRequestClient;
import com.generic.httpclient.cache.LruBitmapCache;
import com.generic.httpclient.common.ILRequest;
import com.generic.httpclient.error.ILError;
import com.generic.httpclient.interfaces.BitmapRequestListener;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by dineshsingh on 12/07/2018.
 */
public class ImageLoader {

    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    private static int cacheSize = maxMemory / 8;
    private static ImageLoader sInstance;
    private final ImageCache mCache;

    private final HashMap<String, BatchedImageRequest> mInFlightRequests =
            new HashMap<String, BatchedImageRequest>();

    private final HashMap<String, BatchedImageRequest> mBatchedResponses =
            new HashMap<String, BatchedImageRequest>();

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private int mBatchResponseDelayMs = 100;
    private Runnable mRunnable;
    private BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();

    public ImageLoader(ImageCache imageCache) {
        mCache = imageCache;
    }

    public static void initialize() {
        getInstance();
    }

    public static void initialize(int mCacheSize) {
        if (mCacheSize < maxMemory) {
            cacheSize = mCacheSize;
        } else
            Log.e(ImageLoader.class.getSimpleName(), "Cache Size Not Valid");
        getInstance();
    }

    public static ImageLoader getInstance() {
        if (sInstance == null) {
            synchronized (ImageLoader.class) {
                if (sInstance == null) {
                    sInstance = new ImageLoader(new LruBitmapCache(cacheSize));
                }
            }
        }
        return sInstance;
    }

    public static ImageListener getImageListener(final ImageView view,
                                                 final int defaultImageResId,
                                                 final int errorImageResId) {
        return new ImageListener() {
            @Override
            public void onResponse(ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    view.setImageBitmap(response.getBitmap());
                } else if (defaultImageResId != 0) {
                    view.setImageResource(defaultImageResId);
                }
            }

            @Override
            public void onError(ILError anError) {
                if (errorImageResId != 0) {
                    view.setImageResource(errorImageResId);
                }
            }
        };
    }

    private static String getCacheKey(String url, int maxWidth, int maxHeight,
                                      ImageView.ScaleType scaleType) {
        return new StringBuilder(url.length() + 12).append("#W").append(maxWidth)
                .append("#H").append(maxHeight).append("#S").append(scaleType.ordinal()).append(url)
                .toString();
    }

    public ImageCache getImageCache() {
        return mCache;
    }

    public boolean isCached(String requestUrl, int maxWidth, int maxHeight) {
        return isCached(requestUrl, maxWidth, maxHeight, ImageView.ScaleType.CENTER_INSIDE);
    }

    public boolean isCached(String requestUrl, int maxWidth, int maxHeight,
                            ImageView.ScaleType scaleType) {
        throwIfNotOnMainThread();

        String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight, scaleType);
        return mCache.getBitmap(cacheKey) != null;
    }

    public ImageContainer get(String requestUrl, final ImageListener listener) {
        return get(requestUrl, listener, 0, 0);
    }

    public ImageContainer get(String requestUrl, ImageListener imageListener,
                              int maxWidth, int maxHeight) {
        return get(requestUrl, imageListener, maxWidth, maxHeight,
                ImageView.ScaleType.CENTER_INSIDE);
    }

    public ImageContainer get(String requestUrl, ImageListener imageListener,
                              int maxWidth, int maxHeight, ImageView.ScaleType scaleType) {

        throwIfNotOnMainThread();

        final String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight, scaleType);

        Bitmap cachedBitmap = mCache.getBitmap(cacheKey);
        if (cachedBitmap != null) {
            ImageContainer container = new ImageContainer(cachedBitmap, requestUrl, null, null);
            imageListener.onResponse(container, true);
            return container;
        }

        ImageContainer imageContainer =
                new ImageContainer(null, requestUrl, cacheKey, imageListener);

        imageListener.onResponse(imageContainer, true);

        BatchedImageRequest request = mInFlightRequests.get(cacheKey);
        if (request != null) {
            request.addContainer(imageContainer);
            return imageContainer;
        }

        ILRequest newRequest = makeImageRequest(requestUrl, maxWidth, maxHeight, scaleType,
                cacheKey);

        mInFlightRequests.put(cacheKey,
                new BatchedImageRequest(newRequest, imageContainer));
        return imageContainer;
    }

    protected ILRequest makeImageRequest(String requestUrl, int maxWidth, int maxHeight,
                                         ImageView.ScaleType scaleType, final String cacheKey) {
        ILRequest ilRequest = HttpRequestClient.get(requestUrl)
                .setTag("RequestImageTag")
                .setBitmapMaxHeight(maxHeight)
                .setBitmapMaxWidth(maxWidth)
                .setImageScaleType(scaleType)
                .setBitmapConfig(Bitmap.Config.RGB_565)
                .setBitmapOptions(mBitmapOptions)
                .build();

        ilRequest.getAsBitmap(new BitmapRequestListener() {
            @Override
            public void onResponse(Bitmap response) {
                onGetImageSuccess(cacheKey, response);
            }

            @Override
            public void onError(ILError anError) {
                onGetImageError(cacheKey, anError);
            }
        });

        return ilRequest;
    }

    public void setBitmapDecodeOptions(BitmapFactory.Options bitmapOptions) {
        mBitmapOptions = bitmapOptions;
    }

    public void setBatchedResponseDelay(int newBatchedResponseDelayMs) {
        mBatchResponseDelayMs = newBatchedResponseDelayMs;
    }

    protected void onGetImageSuccess(String cacheKey, Bitmap response) {
        mCache.putBitmap(cacheKey, response);

        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);

        if (request != null) {
            request.mResponseBitmap = response;

            batchResponse(cacheKey, request);
        }
    }

    protected void onGetImageError(String cacheKey, ILError anError) {
        BatchedImageRequest request = mInFlightRequests.remove(cacheKey);

        if (request != null) {
            request.setError(anError);
            batchResponse(cacheKey, request);
        }
    }

    private void batchResponse(String cacheKey, BatchedImageRequest request) {
        mBatchedResponses.put(cacheKey, request);
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    for (BatchedImageRequest bir : mBatchedResponses.values()) {
                        for (ImageContainer container : bir.mContainers) {
                            if (container.mListener == null) {
                                continue;
                            }
                            if (bir.getError() == null) {
                                container.mBitmap = bir.mResponseBitmap;
                                container.mListener.onResponse(container, false);
                            } else {
                                container.mListener.onError(bir.getError());
                            }
                        }
                    }
                    mBatchedResponses.clear();
                    mRunnable = null;
                }

            };
            mHandler.postDelayed(mRunnable, mBatchResponseDelayMs);
        }
    }

    private void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ImageLoader must be invoked from the main thread.");
        }
    }

    public interface ImageCache {
        Bitmap getBitmap(String key);

        void putBitmap(String key, Bitmap bitmap);

        void evictBitmap(String key);

        void evictAllBitmap();
    }

    public interface ImageListener {

        void onResponse(ImageContainer response, boolean isImmediate);

        void onError(ILError anError);
    }

    public class ImageContainer {

        private final ImageListener mListener;
        private final String mCacheKey;
        private final String mRequestUrl;
        private Bitmap mBitmap;

        public ImageContainer(Bitmap bitmap, String requestUrl,
                              String cacheKey, ImageListener listener) {
            mBitmap = bitmap;
            mRequestUrl = requestUrl;
            mCacheKey = cacheKey;
            mListener = listener;
        }

        public void cancelRequest() {
            if (mListener == null) {
                return;
            }

            BatchedImageRequest request = mInFlightRequests.get(mCacheKey);
            if (request != null) {
                boolean canceled = request.removeContainerAndCancelIfNecessary(this);
                if (canceled) {
                    mInFlightRequests.remove(mCacheKey);
                }
            } else {
                request = mBatchedResponses.get(mCacheKey);
                if (request != null) {
                    request.removeContainerAndCancelIfNecessary(this);
                    if (request.mContainers.size() == 0) {
                        mBatchedResponses.remove(mCacheKey);
                    }
                }
            }
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }


        public String getRequestUrl() {
            return mRequestUrl;
        }
    }

    private class BatchedImageRequest {

        private final ILRequest mRequest;
        private final LinkedList<ImageContainer> mContainers = new LinkedList<ImageContainer>();
        private Bitmap mResponseBitmap;
        private ILError mANError;

        public BatchedImageRequest(ILRequest request, ImageContainer container) {
            mRequest = request;
            mContainers.add(container);
        }

        public ILError getError() {
            return mANError;
        }

        public void setError(ILError anError) {
            mANError = anError;
        }

        public void addContainer(ImageContainer container) {
            mContainers.add(container);
        }

        public boolean removeContainerAndCancelIfNecessary(ImageContainer container) {
            mContainers.remove(container);
            if (mContainers.size() == 0) {
                mRequest.cancel(true);
                if (mRequest.isCanceled()) {
                    mRequest.destroy();
                    ILRequestQueue.getInstance().finish(mRequest);
                }
                return true;
            }
            return false;
        }
    }
}
