package imageloader.task.com.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import imageloader.task.com.R;
import imageloader.task.com.common.ScrollMoreListner;
import imageloader.task.com.model.DetailModel;
import imageloader.task.com.ui.adapter.DetailsAdapter;
import imageloader.task.com.ui.presentor.DetailPresentor;
import imageloader.task.com.ui.view.DetailsViews;
import imageloader.task.com.utils.CommonUtils;

public class LandingActivity extends AppCompatActivity implements DetailsViews, ScrollMoreListner.OnLoadMoreListener {

    private RecyclerView rvDetails;
    private SwipeRefreshLayout srlRefresh;
    private DetailPresentor mPresentor;
    private DetailsAdapter rvAdapter;
    private LinearLayout llNoDataLayout;
    private ScrollMoreListner mScrollingListner;
    private boolean isLoadMoreEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_landing);
        initializePresentor();
        initializeView();
        initializeRecycler();
        initializeAdapter();
        initializeSwipeListner();
        mPresentor.requestData();
    }

    private void initializePresentor() {
        mPresentor = new DetailPresentor(this);
        mPresentor.setView(this);
    }

    private void initializeView() {
        rvDetails = findViewById(R.id.rvDetails);
        srlRefresh = findViewById(R.id.srlRefresh);
        llNoDataLayout = findViewById(R.id.llNoDataLayout);
    }

    private void initializeRecycler() {
        rvDetails.setLayoutManager(new LinearLayoutManager(this));
        rvDetails.setItemAnimator(new DefaultItemAnimator());
        rvDetails.addOnScrollListener(getScrollingListner());
    }

    private RecyclerView.OnScrollListener getScrollingListner() {
        if (mScrollingListner == null) {
            mScrollingListner = new ScrollMoreListner();
            mScrollingListner.setOnLoadMoreListener(this);
            mScrollingListner.setThreshold(10);
        }
        return mScrollingListner;
    }

    private void initializeSwipeListner() {
        srlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                srlRefresh.setRefreshing(true);
                llNoDataLayout.setVisibility(View.GONE);
                mPresentor.requestData();
            }
        });
    }

    private void initializeAdapter() {
        rvAdapter = new DetailsAdapter(this);
        rvDetails.setAdapter(rvAdapter);
    }

    @Override
    public void showProgress() {
        srlRefresh.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        resetLoadMore();
        srlRefresh.setRefreshing(false);
    }

    private void resetLoadMore() {
        if (mScrollingListner.isLoading()) {
            mScrollingListner.setLoading(false);
            rvAdapter.hideLoadProgress();
        }
    }

    @Override
    public void internetError() {
        CommonUtils.initNoDataLayout(llNoDataLayout, getString(R.string.error_no_internet), 0);
        rvDetails.setVisibility(View.GONE);
        llNoDataLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void destroy() {
        mPresentor.destroy();
        mPresentor = null;
    }

    @Override
    public void showData(List<DetailModel> mData) {
        if (mData.size() == 0) {
            CommonUtils.initNoDataLayout(llNoDataLayout, getString(R.string.error_nodata), 0);
            rvDetails.setVisibility(View.GONE);
            llNoDataLayout.setVisibility(View.VISIBLE);
            return;
        }
        if (rvAdapter != null)
            rvAdapter.updateData(mData);
    }

    @Override
    public void showMoreData(List<DetailModel> detailModels) {
        rvAdapter.updateMoreData(detailModels);
        if (rvAdapter.getItemCount() == 0) {
            CommonUtils.initNoDataLayout(llNoDataLayout, getString(R.string.error_nodata), 0);
            rvDetails.setVisibility(View.GONE);
            llNoDataLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadMore() {
        if (srlRefresh.isRefreshing()) {
            mScrollingListner.setLoading(false);
            return;
        }
        rvAdapter.showLoadProgress();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresentor.requestMoreData();
            }
        }, 2000);

    }
}
