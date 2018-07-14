package com.generic.httpclient.interfaces;

import com.generic.httpclient.error.ILError;

/**
 * Created by dineshsingh on 12/07/2018.
 */
public interface StringRequestListener {

    void onResponse(String response);

    void onError(ILError anError);

}
