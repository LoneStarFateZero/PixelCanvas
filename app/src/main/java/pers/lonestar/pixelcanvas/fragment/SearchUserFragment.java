package pers.lonestar.pixelcanvas.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ldoublem.loadingviewlib.view.LVBlazeWood;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.SearchActivity;
import pers.lonestar.pixelcanvas.adapter.SearchUserAdapter;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.listener.MainEndlessRecyclerOnScrollListener;

public class SearchUserFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<PixelUser> pixelUserList = new ArrayList<>();
    private BmobQuery<PixelUser> loadMoreQuery;
    private SearchUserAdapter adapter;
    private SwipeRefreshLayout worldSwipeRefreshLayout;
    private LVBlazeWood lvBlazeWood;
    private View view;
    private int querySkip;
    private int pageLimit = 10;
    private String userName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_world, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        refreshData();
    }

    public void setQueryCanvasName(String userName) {
        this.userName = userName;
    }

    private void initView() {
        recyclerView = view.findViewById(R.id.world_recyclerview);
        worldSwipeRefreshLayout = view.findViewById(R.id.world_swiperefreshlayout);
        lvBlazeWood = view.findViewById(R.id.world_loadinganim);
        //设置网格布局管理器
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        //设置缓存
        recyclerView.setItemViewCacheSize(50);
        //设置子项布局大小不变，省去重新测量过程，提升性能
        recyclerView.setHasFixedSize(true);
        //设置适配器
        adapter = new SearchUserAdapter(pixelUserList);
        recyclerView.setAdapter(adapter);
        //添加滚动监听事件，用于上拉加载
        recyclerView.addOnScrollListener(new MainEndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                loadMoreData();
            }
        });
        //设置刷新监听事件，用于下拉刷新
        worldSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
    }

    private void refreshData() {
        loadingAnimStart();
        //取当前时间对应的BmobDate用于数据查询
        Date currentDate = new Date(System.currentTimeMillis());
        BmobDate bmobCurrentDate = new BmobDate(currentDate);

        //数据查询，时间倒序查询
        querySkip = 0;
        loadMoreQuery = new BmobQuery<>();
        loadMoreQuery.order("-createdAt");
        loadMoreQuery.addWhereEqualTo("nickname", userName);
        //小于当前时间
        loadMoreQuery.addWhereLessThan("createdAt", bmobCurrentDate);
        loadMoreQuery.setLimit(pageLimit);
        loadMoreQuery.setSkip(0);
        loadMoreQuery.findObjects(new FindListener<PixelUser>() {
            @Override
            public void done(List<PixelUser> list, BmobException e) {
                if (e == null) {
                    if (list.isEmpty())
                        adapter.setLoadState(adapter.LOADING_END);
                    else {
                        //下拉刷新前先清空之前的数据
                        pixelUserList.clear();
                        //添加新数据
                        pixelUserList.addAll(list);
                        adapter.notifyDataSetChanged();
                        adapter.setLoadState(adapter.LOADING_COMPLETE);
                    }
                } else {
                    Toast.makeText(SearchActivity.getInstance(), "数据获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
                loadingAnimStop();
            }
        });
    }

    private void loadMoreData() {
        querySkip += pageLimit;
        loadMoreQuery.setSkip(querySkip);
        adapter.setLoadState(adapter.LOADING);
        loadMoreQuery.findObjects(new FindListener<PixelUser>() {
            @Override
            public void done(List<PixelUser> list, BmobException e) {
                if (e == null) {
                    if (list.isEmpty())
                        adapter.setLoadState(adapter.LOADING_END);
                    else {
                        //上拉加载更多不需要清空之前的数据
                        //添加新数据
                        pixelUserList.addAll(list);
                        adapter.setLoadState(adapter.LOADING_COMPLETE);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(SearchActivity.getInstance(), "数据获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //数据加载动画启动
    private void loadingAnimStart() {
        recyclerView.setVisibility(View.INVISIBLE);
        lvBlazeWood.setVisibility(View.VISIBLE);
        worldSwipeRefreshLayout.setRefreshing(false);
        lvBlazeWood.startAnim();
    }

    //数据加载动画停止
    private void loadingAnimStop() {
        lvBlazeWood.stopAnim();
        lvBlazeWood.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}
