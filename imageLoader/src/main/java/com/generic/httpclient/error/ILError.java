package com.generic.httpclient.error;

import com.generic.httpclient.common.ILConstants;

import okhttp3.Response;

/**
 * Created by dineshsingh on 13/07/2018.
 */
@SuppressWarnings({"unchecked", "unused"})
public class ILError extends Exception {

    private String errorBody;

    private int errorCode = 0;

    private String errorDetail;

    private Response response;

    public ILError() {
    }

    public ILError(Response response) {
        this.response = response;
    }

    public ILError(String message) {
        super(message);
    }

    public ILError(String message, Response response) {
        super(message);
        this.response = response;
    }

    public ILError(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ILError(String message, Response response, Throwable throwable) {
        super(message, throwable);
        this.response = response;
    }

    public ILError(Response response, Throwable throwable) {
        super(throwable);
        this.response = response;
    }

    public ILError(Throwable throwable) {
        super(throwable);
    }

    public Response getResponse() {
        return response;
    }

    public String getErrorDetail() {
        return this.errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setCancellationMessageInError() {
        this.errorDetail = ILConstants.ERROR_REQUEST_CANCLED;
    }

    public String getErrorBody() {
        return errorBody;
    }

    public void setErrorBody(String errorBody) {
        this.errorBody = errorBody;
    }

}
