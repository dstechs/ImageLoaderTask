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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.generic.httpclient.widget.ILImageView;

import java.util.ArrayList;
import java.util.List;

import imageloader.task.com.R;
import imageloader.task.com.model.DetailModel;

/**
 * Created by dineshsingh on 7/13/18
 */

public class DetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<DetailModel> mData = new ArrayList<>(0);
    private int animPosition = -1;
    private int VIEW_PROGRESS = 0, VIEW_ITEM = 1;

    public DetailsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void updateData(List<DetailModel> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM)
            return new DetailHolder(LayoutInflater.from(mContext).inflate(R.layout.item_details, parent, false));
        else
            return new ProgressHolder(LayoutInflater.from(mContext).inflate(R.layout.item_progress, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DetailHolder) {
            DetailModel mDetailsModel = mData.get(position);
            DetailHolder mDetailHolder = (DetailHolder) holder;
            mDetailHolder.tvUserId.setText(mDetailsModel.getUser().getUsername());
            mDetailHolder.tvUserName.setText(mDetailsModel.getUser().getName());
            loadImage(mDetailsModel.getUser().getProfileImage().getLarge(), mDetailHolder.ivProfileImage);
            if (animPosition < position) {
                enterAnimation(mDetailHolder);
                animPosition = position;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    private void loadImage(String url, ILImageView ivView) {
        ivView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivView.setDefaultImageResId(R.drawable.ic_image_holder);
        ivView.setErrorImageResId(R.drawable.ic_image_error);
        ivView.setImageUrl(url);
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

    public void hideLoadProgress() {
        mData.remove(mData.size() - 1);
        notifyItemRemoved(mData.size() - 1);
    }

    public void showLoadProgress() {
        mData.add(null);
        notifyItemInserted(mData.size() - 1);
    }

    public void updateMoreData(List<DetailModel> detailModels) {
        mData.addAll(detailModels);
        notifyItemInserted(mData.size());
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

    class ProgressHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.pbProgress);
        }
    }
}
