package com.generic.httpclient.core;

/**
 * Created by dineshsingh on 11/07/2018.
 */
public class Core {

    private static Core sInstance = null;
    private final ExecutorSupplier mExecutorSupplier;

    private Core() {
        this.mExecutorSupplier = new DefaultExecutorSupplier();
    }

    public static Core getInstance() {
        if (sInstance == null) {
            synchronized (Core.class) {
                if (sInstance == null) {
                    sInstance = new Core();
                }
            }
        }
        return sInstance;
    }

    public static void shutDown() {
        if (sInstance != null) {
            sInstance = null;
        }
    }

    public ExecutorSupplier getExecutorSupplier() {
        return mExecutorSupplier;
    }
}
