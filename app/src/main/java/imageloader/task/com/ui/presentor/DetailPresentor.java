package imageloader.task.com.ui.presentor;

import android.content.Context;

import com.generic.httpclient.HttpRequestClient;
import com.generic.httpclient.error.ILError;
import com.generic.httpclient.interfaces.StringRequestListener;

import imageloader.task.com.model.DetailModel;
import imageloader.task.com.provider.Constant;
import imageloader.task.com.ui.view.DetailsViews;
import imageloader.task.com.utils.JsonModelParser;
import imageloader.task.com.utils.NetworkUtil;

/**
 * Created by dineshsingh on 7/13/18
 */

public class DetailPresentor {

    Context mContext;
    DetailsViews uiView;
    JsonModelParser mParser;

    public DetailPresentor(Context mContext) {
        this.mContext = mContext;
        mParser = new JsonModelParser();
    }

    public void setView(DetailsViews uiView) {
        this.uiView = uiView;
    }

    public void requestData() {
        if (!NetworkUtil.isAvailable(mContext)) {
            uiView.hideProgress();
            uiView.internetError();
            return;
        }
        HttpRequestClient.get(Constant.DETAILS_URL).setTag(DetailPresentor.class).build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                if (uiView == null)
                    return;
                uiView.hideProgress();
                if (null != response && !response.isEmpty() && response.trim().startsWith("[")) {
                    uiView.showData(mParser.<DetailModel>parseCollection(response, DetailModel.class));
                }
            }

            @Override
            public void onError(ILError anError) {
                if (uiView == null)
                    return;
                uiView.hideProgress();
            }
        });
    }

    public void destroy() {
        HttpRequestClient.cancel(DetailPresentor.class);
        mParser = null;
        mContext = null;
        uiView = null;
    }

}
