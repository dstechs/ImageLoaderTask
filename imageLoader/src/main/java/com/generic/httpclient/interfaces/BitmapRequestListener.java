package com.generic.httpclient.interfaces;

import android.graphics.Bitmap;

import com.generic.httpclient.error.ILError;

/**
 * Created by dineshsingh on 12/07/2018.
 */
public interface BitmapRequestListener {

    void onResponse(Bitmap response);

    void onError(ILError anError);

}
