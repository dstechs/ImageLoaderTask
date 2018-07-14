package com.generic.httpclient.core;

import java.util.concurrent.Executor;

/**
 * Created by dineshsingh on 11/07/2018.
 */
public interface ExecutorSupplier {

    ILExecutor forNetworkTasks();

    ILExecutor forImmediateNetworkTasks();

    Executor forMainThreadTasks();
}
