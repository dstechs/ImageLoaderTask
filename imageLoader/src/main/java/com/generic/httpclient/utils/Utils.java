package com.generic.httpclient.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.generic.httpclient.common.ILConstants;
import com.generic.httpclient.common.ILRequest;
import com.generic.httpclient.common.ILResponse;
import com.generic.httpclient.error.ILError;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;

import okhttp3.Cache;
import okhttp3.Response;
import okio.Okio;

/**
 * Created by dineshsingh on 11/07/2018.
 */
public class Utils {

    public static File getDiskCacheDir(Context context, String uniqueName) {
        return new File(context.getCacheDir(), uniqueName);
    }

    public static Cache getCache(Context context, int maxCacheSize, String uniqueName) {
        return new Cache(getDiskCacheDir(context, uniqueName), maxCacheSize);
    }

    public static String getMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    public static ILResponse<Bitmap> decodeBitmap(Response response, int maxWidth,
                                                  int maxHeight, Bitmap.Config decodeConfig,
                                                  ImageView.ScaleType scaleType) {
        return decodeBitmap(response, maxWidth, maxHeight, decodeConfig,
                new BitmapFactory.Options(), scaleType);
    }

    public static ILResponse<Bitmap> decodeBitmap(Response response, int maxWidth,
                                                  int maxHeight, Bitmap.Config decodeConfig,
                                                  BitmapFactory.Options decodeOptions,
                                                  ImageView.ScaleType scaleType) {
        byte[] data = new byte[0];
        try {
            data = Okio.buffer(response.body().source()).readByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = null;
        if (maxWidth == 0 && maxHeight == 0) {
            decodeOptions.inPreferredConfig = decodeConfig;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
        } else {
            decodeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;

            int desiredWidth = getResizedDimension(maxWidth, maxHeight,
                    actualWidth, actualHeight, scaleType);
            int desiredHeight = getResizedDimension(maxHeight, maxWidth,
                    actualHeight, actualWidth, scaleType);

            decodeOptions.inJustDecodeBounds = false;
            decodeOptions.inSampleSize =
                    findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap =
                    BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);

            if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth ||
                    tempBitmap.getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap,
                        desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }

        if (bitmap == null) {
            return ILResponse.failed(Utils.getErrorForParse(new ILError(response)));
        } else {
            return ILResponse.success(bitmap);
        }
    }

    private static int getResizedDimension(int maxPrimary, int maxSecondary,
                                           int actualPrimary, int actualSecondary,
                                           ImageView.ScaleType scaleType) {

        if ((maxPrimary == 0) && (maxSecondary == 0)) {
            return actualPrimary;
        }

        if (scaleType == ImageView.ScaleType.FIT_XY) {
            if (maxPrimary == 0) {
                return actualPrimary;
            }
            return maxPrimary;
        }

        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0) {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;

        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
            if ((resized * ratio) < maxSecondary) {
                resized = (int) (maxSecondary / ratio);
            }
            return resized;
        }

        if ((resized * ratio) > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }

    public static int findBestSampleSize(int actualWidth, int actualHeight,
                                         int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }
        return (int) n;
    }

    public static ILError getErrorForConnection(ILError error) {
        error.setErrorDetail(ILConstants.ERROR_CONNECTION);
        error.setErrorCode(0);
        return error;
    }


    public static ILError getErrorForServerResponse(ILError error, ILRequest request, int code) {
        error = request.parseNetworkError(error);
        error.setErrorCode(code);
        error.setErrorDetail(ILConstants.ERROR_FROM_SERVER);
        return error;
    }

    public static ILError getErrorForParse(ILError error) {
        error.setErrorCode(0);
        error.setErrorDetail(ILConstants.ERROR_PARSE);
        return error;
    }

}
