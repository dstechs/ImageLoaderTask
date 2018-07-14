package com.generic.httpclient.internal;

import com.generic.httpclient.common.ILRequest;
import com.generic.httpclient.common.ILResponse;
import com.generic.httpclient.error.ILError;
import com.generic.httpclient.utils.CloseRequestUtils;
import com.generic.httpclient.utils.Utils;

import okhttp3.Response;

import static com.generic.httpclient.common.RequestType.SIMPLE;

/**
 * Created by dineshsingh on 12/07/2018.
 */
@SuppressWarnings("unchecked")
public final class SyncCall {

    private SyncCall() {

    }

    public static <T> ILResponse<T> execute(ILRequest request) {
        switch (request.getRequestType()) {
            case SIMPLE:
                return executeSimpleRequest(request);
        }
        return new ILResponse<>(new ILError());
    }

    private static <T> ILResponse<T> executeSimpleRequest(ILRequest request) {
        Response okHttpResponse = null;
        try {
            okHttpResponse = Networking.performRequest(request);
            if (okHttpResponse == null) {
                return new ILResponse<>(Utils.getErrorForConnection(new ILError()));
            }

            if (okHttpResponse.code() >= 400) {
                ILResponse response = new ILResponse<>(Utils.getErrorForServerResponse(new ILError(okHttpResponse),
                        request, okHttpResponse.code()));
                response.setOkHttpResponse(okHttpResponse);
                return response;
            }
            ILResponse response = request.parseResponse(okHttpResponse);
            response.setOkHttpResponse(okHttpResponse);
            return response;
        } catch (ILError se) {
            return new ILResponse<>(Utils.getErrorForConnection(new ILError(se)));
        } catch (Exception e) {
            return new ILResponse<>(Utils.getErrorForConnection(new ILError(e)));
        } finally {
            CloseRequestUtils.close(okHttpResponse);
        }
    }

}
