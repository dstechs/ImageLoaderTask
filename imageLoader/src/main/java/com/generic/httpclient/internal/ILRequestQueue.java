package com.generic.httpclient.internal;

import com.generic.httpclient.common.ILRequest;
import com.generic.httpclient.common.Priority;
import com.generic.httpclient.core.Core;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dineshsingh on 12/07/2018.
 */
public class ILRequestQueue {

    private static ILRequestQueue sInstance = null;
    private final Set<ILRequest> mCurrentRequests =
            Collections.newSetFromMap(new ConcurrentHashMap<ILRequest, Boolean>());
    private AtomicInteger mSequenceGenerator = new AtomicInteger();

    public static void initialize() {
        getInstance();
    }

    public static ILRequestQueue getInstance() {
        if (sInstance == null) {
            synchronized (ILRequestQueue.class) {
                if (sInstance == null) {
                    sInstance = new ILRequestQueue();
                }
            }
        }
        return sInstance;
    }

    private void cancel(RequestFilter filter, boolean forceCancel) {
        try {
            for (Iterator<ILRequest> iterator = mCurrentRequests.iterator(); iterator.hasNext(); ) {
                ILRequest request = iterator.next();
                if (filter.apply(request)) {
                    request.cancel(forceCancel);
                    if (request.isCanceled()) {
                        request.destroy();
                        iterator.remove();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelAll(boolean forceCancel) {
        try {
            for (Iterator<ILRequest> iterator = mCurrentRequests.iterator(); iterator.hasNext(); ) {
                ILRequest request = iterator.next();
                request.cancel(forceCancel);
                if (request.isCanceled()) {
                    request.destroy();
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelRequestWithGivenTag(final Object tag, final boolean forceCancel) {
        try {
            if (tag == null) {
                return;
            }
            cancel(new RequestFilter() {
                @Override
                public boolean apply(ILRequest request) {
                    return isRequestWithTheGivenTag(request, tag);
                }
            }, forceCancel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getSequenceNumber() {
        return mSequenceGenerator.incrementAndGet();
    }

    public ILRequest addRequest(ILRequest request) {
        try {
            mCurrentRequests.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            request.setSequenceNumber(getSequenceNumber());
            if (request.getPriority() == Priority.IMMEDIATE) {
                request.setFuture(Core.getInstance()
                        .getExecutorSupplier()
                        .forImmediateNetworkTasks()
                        .submit(new FutureRunnable(request)));
            } else {
                request.setFuture(Core.getInstance()
                        .getExecutorSupplier()
                        .forNetworkTasks()
                        .submit(new FutureRunnable(request)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }

    public void finish(ILRequest request) {
        try {
            mCurrentRequests.remove(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRequestRunning(Object tag) {
        try {
            for (ILRequest request : mCurrentRequests) {
                if (isRequestWithTheGivenTag(request, tag) && request.isRunning()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isRequestWithTheGivenTag(ILRequest request, Object tag) {
        if (request.getTag() == null) {
            return false;
        }
        if (request.getTag() instanceof String && tag instanceof String) {
            final String tempRequestTag = (String) request.getTag();
            final String tempTag = (String) tag;
            return tempRequestTag.equals(tempTag);
        }
        return request.getTag().equals(tag);
    }

    public interface RequestFilter {
        boolean apply(ILRequest request);
    }

}
