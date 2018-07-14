package imageloader.task.com.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import imageloader.task.com.R;

/**
 * Created by dinesh on 7/14/18.
 */

public class CommonUtils {

    public static void initNoDataLayout(View parent, String msg, int imageId) {

        if (null != parent) {
            TextView tvMsg = parent.findViewById(R.id.tvErrorMsg);
            ImageView image = parent.findViewById(R.id.ivErrorImg);
            if (null != tvMsg)
                tvMsg.setText(msg);

            if (imageId > 0) {
                image.setVisibility(View.VISIBLE);
                image.setImageResource(imageId);
            } else
                image.setVisibility(View.GONE);
        }
    }

}
