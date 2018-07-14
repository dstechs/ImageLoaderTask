package com.generic.httpclient.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.generic.httpclient.core.Core;
import com.generic.httpclient.error.ILError;
import com.generic.httpclient.interfaces.BitmapRequestListener;
import com.generic.httpclient.interfaces.StringRequestListener;
import com.generic.httpclient.internal.ILRequestQueue;
import com.generic.httpclient.internal.SyncCall;
import com.generic.httpclient.utils.Utils;

import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.Okio;

/**
 * Created by dineshsingh on 11/07/2018.
 */
@SuppressWarnings({"unchecked", "unused"})
public class ILRequest<T extends ILRequest> {

    private final static String TAG = ILRequest.class.getSimpleName();
    private static final Object sDecodeLock = new Object();
    private int mMethod;
    private Priority mPriority;
    private int mRequestType;
    private String mUrl;
    private int sequenceNumber;
    private Object mTag;
    private ResponseType mResponseType;
    private Future future;
    private Call call;
    private int mProgress;
    private boolean isCancelled;
    private boolean isDelivered;
    private boolean isRunning;
    private int mPercentageThresholdForCancelling = 0;
    private BitmapRequestListener mBitmapRequestListener;
    private StringRequestListener mStringRequestListener;

    private Bitmap.Config mDecodeConfig;
    private int mMaxWidth;
    private int mMaxHeight;
    private ImageView.ScaleType mScaleType;
    private CacheControl mCacheControl = null;
    private Executor mExecutor = null;
    private OkHttpClient mOkHttpClient = null;
    private String mUserAgent = null;
    private Type mType = null;

    public ILRequest(ILRequestBuilder builder) {
        this.mRequestType = RequestType.SIMPLE;
        this.mMethod = builder.mMethod;
        this.mPriority = builder.mPriority;
        this.mUrl = builder.mUrl;
        this.mTag = builder.mTag;
        this.mDecodeConfig = builder.mDecodeConfig;
        this.mMaxHeight = builder.mMaxHeight;
        this.mMaxWidth = builder.mMaxWidth;
        this.mScaleType = builder.mScaleType;
        this.mCacheControl = builder.mCacheControl;
        this.mExecutor = builder.mExecutor;
        this.mOkHttpClient = builder.mOkHttpClient;
        this.mUserAgent = builder.mUserAgent;
    }

    public void getAsBitmap(BitmapRequestListener requestListener) {
        this.mResponseType = ResponseType.BITMAP;
        this.mBitmapRequestListener = requestListener;
        ILRequestQueue.getInstance().addRequest(this);
    }

    public void getAsString(StringRequestListener requestListener) {
        this.mResponseType = ResponseType.STRING;
        this.mStringRequestListener = requestListener;
        ILRequestQueue.getInstance().addRequest(this);
    }

    public ILResponse executeForString() {
        this.mResponseType = ResponseType.STRING;
        return SyncCall.execute(this);
    }

    public ILResponse executeForBitmap() {
        this.mResponseType = ResponseType.BITMAP;
        return SyncCall.execute(this);
    }

    public int getMethod() {
        return mMethod;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public String getUrl() {
        String tempUrl = mUrl;
        HttpUrl.Builder urlBuilder = HttpUrl.parse(tempUrl).newBuilder();
        return urlBuilder.build().toString();
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
    }

    public ResponseType getResponseAs() {
        return mResponseType;
    }

    public void setResponseAs(ResponseType responseType) {
        this.mResponseType = responseType;
    }

    public Object getTag() {
        return mTag;
    }

    public int getRequestType() {
        return mRequestType;
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public String getUserAgent() {
        return mUserAgent;
    }

    public void setUserAgent(String userAgent) {
        this.mUserAgent = userAgent;
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        this.mType = type;
    }

    public CacheControl getCacheControl() {
        return mCacheControl;
    }

    public ImageView.ScaleType getScaleType() {
        return mScaleType;
    }

    public void cancel(boolean forceCancel) {
        try {
            if (forceCancel || mPercentageThresholdForCancelling == 0
                    || mProgress < mPercentageThresholdForCancelling) {
                isCancelled = true;
                isRunning = false;
                if (call != null) {
                    call.cancel();
                }
                if (future != null) {
                    future.cancel(true);
                }
                if (!isDelivered) {
                    deliverError(new ILError());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCanceled() {
        return isCancelled;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public Call getCall() {
        return call;
    }

    public void setCall(Call call) {
        this.call = call;
    }

    public Future getFuture() {
        return future;
    }

    public void setFuture(Future future) {
        this.future = future;
    }

    public void destroy() {
        mBitmapRequestListener = null;
    }

    public void finish() {
        destroy();
        ILRequestQueue.getInstance().finish(this);
    }

    public ILResponse parseResponse(Response response) {
        switch (mResponseType) {
            case BITMAP:
                synchronized (sDecodeLock) {
                    try {
                        return Utils.decodeBitmap(response, mMaxWidth, mMaxHeight,
                                mDecodeConfig, mScaleType);
                    } catch (Exception e) {
                        return ILResponse.failed(Utils.getErrorForParse(new ILError(e)));
                    }
                }
            case STRING:
                try {
                    return ILResponse.success(Okio.buffer(response
                            .body().source()).readUtf8());
                } catch (Exception e) {
                    return ILResponse.failed(Utils.getErrorForParse(new ILError(e)));
                }
        }
        return null;
    }

    public ILError parseNetworkError(ILError anError) {
        try {
            if (anError.getResponse() != null && anError.getResponse().body() != null
                    && anError.getResponse().body().source() != null) {
                anError.setErrorBody(Okio.buffer(anError
                        .getResponse().body().source()).readUtf8());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return anError;
    }

    public synchronized void deliverError(ILError anError) {
        try {
            if (!isDelivered) {
                if (isCancelled) {
                    anError.setCancellationMessageInError();
                    anError.setErrorCode(0);
                }
                deliverErrorResponse(anError);
            }
            isDelivered = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void deliverResponse(final ILResponse response) {
        try {
            isDelivered = true;
            if (!isCancelled) {
                if (mExecutor != null) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            deliverSuccessResponse(response);
                        }
                    });
                } else {
                    Core.getInstance().getExecutorSupplier().forMainThreadTasks().execute(new Runnable() {
                        public void run() {
                            deliverSuccessResponse(response);
                        }
                    });
                }
            } else {
                ILError anError = new ILError();
                anError.setCancellationMessageInError();
                anError.setErrorCode(0);
                deliverErrorResponse(anError);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deliverSuccessResponse(ILResponse response) {
        if (mBitmapRequestListener != null) {
            mBitmapRequestListener.onResponse((Bitmap) response.getResult());
        } else if (mStringRequestListener != null) {
            mStringRequestListener.onResponse((String) response.getResult());
        }
        finish();
    }

    private void deliverErrorResponse(ILError anError) {
        if (mBitmapRequestListener != null) {
            mBitmapRequestListener.onError(anError);
        } else if (mStringRequestListener != null) {
            mStringRequestListener.onError(anError);
        }
    }

    @Override
    public String toString() {
        return "ILRequest{" +
                "sequenceNumber='" + sequenceNumber +
                ", mMethod=" + mMethod +
                ", mPriority=" + mPriority +
                ", mRequestType=" + mRequestType +
                ", mUrl=" + mUrl +
                '}';
    }

    public static class ILRequestBuilder<T extends ILRequestBuilder> implements RequestBuilder {
        private Priority mPriority = Priority.MEDIUM;
        private int mMethod = Method.GET;
        private String mUrl;
        private Object mTag;
        private Bitmap.Config mDecodeConfig;
        private BitmapFactory.Options mBitmapOptions;
        private int mMaxWidth;
        private int mMaxHeight;
        private ImageView.ScaleType mScaleType;
        private CacheControl mCacheControl;
        private Executor mExecutor;
        private OkHttpClient mOkHttpClient;
        private String mUserAgent;

        public ILRequestBuilder(String url) {
            this.mUrl = url;
            this.mMethod = Method.GET;
        }

        public ILRequestBuilder(String url, int method) {
            this.mUrl = url;
            this.mMethod = method;
        }

        @Override
        public T setPriority(Priority priority) {
            mPriority = priority;
            return (T) this;
        }

        @Override
        public T setTag(Object tag) {
            mTag = tag;
            return (T) this;
        }

        @Override
        public T doNotCacheResponse() {
            mCacheControl = new CacheControl.Builder().noStore().build();
            return (T) this;
        }

        @Override
        public T getResponseOnlyIfCached() {
            mCacheControl = CacheControl.FORCE_CACHE;
            return (T) this;
        }

        @Override
        public T getResponseOnlyFromNetwork() {
            mCacheControl = CacheControl.FORCE_NETWORK;
            return (T) this;
        }

        @Override
        public T setExecutor(Executor executor) {
            mExecutor = executor;
            return (T) this;
        }

        @Override
        public T setUserAgent(String userAgent) {
            mUserAgent = userAgent;
            return (T) this;
        }

        public T setBitmapConfig(Bitmap.Config bitmapConfig) {
            mDecodeConfig = bitmapConfig;
            return (T) this;
        }

        public T setBitmapOptions(BitmapFactory.Options bitmapOptions) {
            mBitmapOptions = bitmapOptions;
            return (T) this;
        }

        public T setBitmapMaxHeight(int maxHeight) {
            mMaxHeight = maxHeight;
            return (T) this;
        }

        public T setBitmapMaxWidth(int maxWidth) {
            mMaxWidth = maxWidth;
            return (T) this;
        }

        public T setImageScaleType(ImageView.ScaleType imageScaleType) {
            mScaleType = imageScaleType;
            return (T) this;
        }

        public ILRequest build() {
            return new ILRequest(this);
        }
    }
}
