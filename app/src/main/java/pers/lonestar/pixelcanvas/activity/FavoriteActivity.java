package pers.lonestar.pixelcanvas.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ldoublem.loadingviewlib.view.LVBlazeWood;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.adapter.FavoriteAdapter;
import pers.lonestar.pixelcanvas.infostore.CanvasFavorite;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.listener.FavoriteRecyclerOnScrollListener;

public class FavoriteActivity extends BaseSwipeBackActivity {
    private static FavoriteActivity instance;
    private RecyclerView recyclerView;
    private LVBlazeWood lvBlazeWood;
    private FavoriteAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private PixelUser pixelUser;
    private List<CanvasFavorite> favoriteList;
    //用于查询收藏列表
    private BmobQuery<CanvasFavorite> loadMoreFavoriteQuery;
    private int pageLimit = 10;
    private int querySkip = 0;

    public static FavoriteActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        instance = this;

        initView();
        refreshData();
    }

    private void initView() {
        pixelUser = BmobUser.getCurrentUser(PixelUser.class);

        toolbar = (Toolbar) findViewById(R.id.favorite_activity_toolbar);
        toolbar.setTitle("收藏列表");
        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.favorite_activity_SwipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.favorite_activity_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteList = new ArrayList<>();
        adapter = new FavoriteAdapter(favoriteList);
        recyclerView.setAdapter(adapter);
        //设置缓存
        recyclerView.setItemViewCacheSize(50);
        //设置子项布局大小不变，省去重新测量过程，提升性能
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new FavoriteRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                //获取更多数据
                loadMoreData();
            }
        });

        lvBlazeWood = (LVBlazeWood) findViewById(R.id.favorite_activity_loadinganim);
    }

    private void refreshData() {
        loadingAnimStart();
        querySkip = 0;
        loadMoreFavoriteQuery = new BmobQuery<>();
        loadMoreFavoriteQuery.setLimit(pageLimit);
        loadMoreFavoriteQuery.order("-createdAt");
        loadMoreFavoriteQuery.include("canvas,creator");
        loadMoreFavoriteQuery.addWhereEqualTo("favoriteUser", pixelUser);
        loadMoreFavoriteQuery.findObjects(new FindListener<CanvasFavorite>() {
            @Override
            public void done(List<CanvasFavorite> list, BmobException e) {
                swipeRefreshLayout.setRefreshing(false);
                if (e == null) {
                    favoriteList.clear();
                    favoriteList.addAll(list);
                    adapter.notifyDataSetChanged();
                    if (list.isEmpty())
                        adapter.setLoadState(adapter.LOADING_END);
                    else {
                        adapter.setLoadState(adapter.LOADING_COMPLETE);
                    }
                    loadingAnimStop();
                } else
                    Toast.makeText(FavoriteActivity.this, "收藏列表获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMoreData() {
        querySkip += pageLimit;
        adapter.setLoadState(adapter.LOADING);
        loadMoreFavoriteQuery.setSkip(querySkip);
        loadMoreFavoriteQuery.findObjects(new FindListener<CanvasFavorite>() {
            @Override
            public void done(List<CanvasFavorite> list, BmobException e) {
                swipeRefreshLayout.setRefreshing(false);
                if (e == null) {
                    if (list.isEmpty()) {
                        adapter.setLoadState(adapter.LOADING_END);
                    } else {
                        favoriteList.addAll(list);
                        adapter.notifyDataSetChanged();
                        adapter.setLoadState(adapter.LOADING_COMPLETE);
                    }
                } else
                    Toast.makeText(FavoriteActivity.this, "收藏列表获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //数据加载动画启动
    private void loadingAnimStart() {
        recyclerView.setVisibility(View.INVISIBLE);
        lvBlazeWood.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
        lvBlazeWood.startAnim();
    }

    //数据加载动画停止
    private void loadingAnimStop() {
        lvBlazeWood.stopAnim();
        lvBlazeWood.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}
