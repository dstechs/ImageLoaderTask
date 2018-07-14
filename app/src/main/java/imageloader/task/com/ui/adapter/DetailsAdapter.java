package imageloader.task.com.ui.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.generic.httpclient.widget.ILImageView;

import java.util.ArrayList;
import java.util.List;

import imageloader.task.com.R;
import imageloader.task.com.model.DetailModel;

/**
 * Created by dineshsingh on 7/13/18
 */

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.DetailHolder> {

    private Context mContext;
    private List<DetailModel> mData = new ArrayList<>(0);
    private int animPosition = -1;

    public DetailsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void updateData(List<DetailModel> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DetailHolder(LayoutInflater.from(mContext).inflate(R.layout.item_details, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DetailHolder holder, int position) {
        DetailModel mDetailsModel = mData.get(position);
        holder.tvUserId.setText(mDetailsModel.getUser().getUsername());
        holder.tvUserName.setText(mDetailsModel.getUser().getName());
        loadImage(mDetailsModel.getUser().getProfileImage().getLarge(), holder.ivProfileImage);
        enterAnimation(holder);
        if (animPosition < position) {
            enterAnimation(holder);
            animPosition = position;
        }
    }

    private void loadImage(String url, ILImageView ivView) {
        ivView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivView.setDefaultImageResId(R.drawable.ic_image_holder);
        ivView.setErrorImageResId(R.drawable.ic_image_error);
        ivView.setImageUrl(url);
//        ImageLoader.getInstance().get(url, getImageListener(ivView, R.drawable.ic_image_holder, R.drawable.ic_image_error));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private void enterAnimation(DetailHolder holder) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(holder.itemView, "translationY", 250, 0);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(300);
        animator.start();
    }

    class DetailHolder extends RecyclerView.ViewHolder {
        private ILImageView ivProfileImage;
        private TextView tvUserName;
        private TextView tvUserId;

        public DetailHolder(View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserId = itemView.findViewById(R.id.tvUserId);
        }

    }
}
