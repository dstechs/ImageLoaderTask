package com.generic.httpclient.internal;

import com.generic.httpclient.common.ILRequest;
import com.generic.httpclient.common.ILResponse;
import com.generic.httpclient.common.Priority;
import com.generic.httpclient.common.RequestType;
import com.generic.httpclient.core.Core;
import com.generic.httpclient.error.ILError;
import com.generic.httpclient.utils.CloseRequestUtils;
import com.generic.httpclient.utils.Utils;

import okhttp3.Response;

/**
 * Created by dineshsingh on 12/07/2018.
 */
public class FutureRunnable implements Runnable {

    public final int sequence;
    public final ILRequest request;
    private final Priority priority;

    public FutureRunnable(ILRequest request) {
        this.request = request;
        this.sequence = request.getSequenceNumber();
        this.priority = request.getPriority();
    }

    @Override
    public void run() {
        request.setRunning(true);
        switch (request.getRequestType()) {
            case RequestType.SIMPLE:
                executeSimpleRequest();
                break;
        }
        request.setRunning(false);
    }

    private void executeSimpleRequest() {
        Response okHttpResponse = null;
        try {
            okHttpResponse = Networking.performRequest(request);

            if (okHttpResponse == null) {
                deliverError(request, Utils.getErrorForConnection(new ILError()));
                return;
            }

            if (okHttpResponse.code() >= 400) {
                deliverError(request, Utils.getErrorForServerResponse(new ILError(okHttpResponse),
                        request, okHttpResponse.code()));
                return;
            }

            ILResponse response = request.parseResponse(okHttpResponse);
            if (!response.isSuccess()) {
                deliverError(request, response.getError());
                return;
            }
            response.setOkHttpResponse(okHttpResponse);
            request.deliverResponse(response);
        } catch (Exception e) {
            deliverError(request, Utils.getErrorForConnection(new ILError(e)));
        } finally {
            CloseRequestUtils.close(okHttpResponse);
        }
    }

    public Priority getPriority() {
        return priority;
    }

    private void deliverError(final ILRequest request, final ILError anError) {
        Core.getInstance().getExecutorSupplier().forMainThreadTasks().execute(new Runnable() {
            public void run() {
                request.deliverError(anError);
                request.finish();
            }
        });
    }
}
