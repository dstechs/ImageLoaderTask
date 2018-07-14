package imageloader.task.com.utils;

import android.app.Application;
import android.graphics.BitmapFactory;

import com.generic.httpclient.HttpRequestClient;

/**
 * Created by dineshsingh on 7/13/18
 */

public class TaskApplication extends Application {

    private static final String TAG = TaskApplication.class.getSimpleName();
    private static TaskApplication appInstance = null;

    public static TaskApplication getInstance() {
        return appInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        HttpRequestClient.initialize(getApplicationContext());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        HttpRequestClient.setBitmapDecodeOptions(options);
        HttpRequestClient.enableLogging();
    }


}
