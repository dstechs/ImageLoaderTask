package com.generic.httpclient.core;

import android.os.Process;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * Created by dineshsingh on 11/07/2018.
 */
public class DefaultExecutorSupplier implements ExecutorSupplier {

    public static final int DEFAULT_MAX_NUM_THREADS = 2 * Runtime.getRuntime().availableProcessors() + 1;
    private final ILExecutor mNetworkExecutor;
    private final ILExecutor mImmediateNetworkExecutor;
    private final Executor mMainThreadExecutor;

    public DefaultExecutorSupplier() {
        ThreadFactory backgroundPriorityThreadFactory = new PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND);
        mNetworkExecutor = new ILExecutor(DEFAULT_MAX_NUM_THREADS, backgroundPriorityThreadFactory);
        mImmediateNetworkExecutor = new ILExecutor(2, backgroundPriorityThreadFactory);
        mMainThreadExecutor = new MainThreadExecutor();
    }

    @Override
    public ILExecutor forNetworkTasks() {
        return mNetworkExecutor;
    }

    @Override
    public ILExecutor forImmediateNetworkTasks() {
        return mImmediateNetworkExecutor;
    }

    @Override
    public Executor forMainThreadTasks() {
        return mMainThreadExecutor;
    }
}
