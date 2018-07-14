package com.generic.httpclient.common;

import com.generic.httpclient.error.ILError;

import okhttp3.Response;

/**
 * Created by dineshsingh on 11/07/2018.
 */
public class ILResponse<T> {

    private final T mResult;

    private final ILError mANError;

    private Response response;

    public ILResponse(T result) {
        this.mResult = result;
        this.mANError = null;
    }

    public ILResponse(ILError anError) {
        this.mResult = null;
        this.mANError = anError;
    }

    public static <T> ILResponse<T> success(T result) {
        return new ILResponse<>(result);
    }

    public static <T> ILResponse<T> failed(ILError anError) {
        return new ILResponse<>(anError);
    }

    public T getResult() {
        return mResult;
    }

    public boolean isSuccess() {
        return mANError == null;
    }

    public ILError getError() {
        return mANError;
    }

    public Response getOkHttpResponse() {
        return response;
    }

    public void setOkHttpResponse(Response response) {
        this.response = response;
    }

}
