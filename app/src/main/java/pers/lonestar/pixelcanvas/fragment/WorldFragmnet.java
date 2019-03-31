package pers.lonestar.pixelcanvas.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.MainActivity;
import pers.lonestar.pixelcanvas.adapter.MainCanvasAdapter;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.listener.MainEndlessRecyclerOnScrollListener;

public class WorldFragmnet extends Fragment {
    List<BmobCanvas> bmobCanvasList = new ArrayList<>();
    private RecyclerView recyclerView;
    private BmobQuery<BmobCanvas> loadMoreQuery;
    private MainCanvasAdapter adapter;
    private SwipeRefreshLayout worldSwipeRefreshLayout;
    private SwipeRefreshLayout watchSwipeRefreshLayout;
    private View view;
    private int querySkip;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


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

    private void initView() {
        recyclerView = view.findViewById(R.id.world_recyclerview);
        worldSwipeRefreshLayout = view.findViewById(R.id.world_swiperefreshlayout);
        //设置网格布局管理器
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.getInstance(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        //设置适配器
        adapter = new MainCanvasAdapter(bmobCanvasList);
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
        //取当前时间对应的BmobDate用于数据查询
        Date currentDate = new Date(System.currentTimeMillis());
        BmobDate bmobCurrentDate = new BmobDate(currentDate);

        //数据查询，时间倒序查询
        querySkip = 0;
        loadMoreQuery = new BmobQuery<>();
        loadMoreQuery.order("-createdAt");
        //小于当前时间
        loadMoreQuery.addWhereLessThan("createdAt", bmobCurrentDate);
        loadMoreQuery.setLimit(10);
        loadMoreQuery.setSkip(0);
        loadMoreQuery.findObjects(new FindListener<BmobCanvas>() {
            @Override
            public void done(List<BmobCanvas> list, BmobException e) {
                if (e == null) {
                    if (list.isEmpty())
                        adapter.setLoadState(adapter.LOADING_END);
                    else {
                        querySkip += list.size();
                        loadMoreQuery.setSkip(querySkip);
                        //下拉刷新前先清空之前的数据
                        bmobCanvasList.clear();
                        //添加新数据
                        bmobCanvasList.addAll(list);
                        adapter.setLoadState(adapter.LOADING_COMPLETE);
                    }
                    worldSwipeRefreshLayout.setRefreshing(false);
                } else {
                    Toast.makeText(MainActivity.getInstance(), "数据获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMoreData() {
        adapter.setLoadState(adapter.LOADING);
        loadMoreQuery.findObjects(new FindListener<BmobCanvas>() {
            @Override
            public void done(List<BmobCanvas> list, BmobException e) {
                if (e == null) {
                    if (list.isEmpty())
                        adapter.setLoadState(adapter.LOADING_END);
                    else {
                        querySkip += list.size();
                        loadMoreQuery.setSkip(querySkip);
                        //上拉加载更多不需要清空之前的数据
                        //添加新数据
                        bmobCanvasList.addAll(list);
                        adapter.setLoadState(adapter.LOADING_COMPLETE);
                    }
                } else {
                    Toast.makeText(MainActivity.getInstance(), "数据获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
