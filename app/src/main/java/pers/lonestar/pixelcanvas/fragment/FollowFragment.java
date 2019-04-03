package pers.lonestar.pixelcanvas.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ldoublem.loadingviewlib.view.LVBlazeWood;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.MainActivity;
import pers.lonestar.pixelcanvas.adapter.FollowCanvasAdapter;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.listener.MainEndlessRecyclerOnScrollListener;

public class FollowFragment extends Fragment {
    //用于存储关注用户的作品
    private List<BmobCanvas> bmobCanvasList = new ArrayList<>();
    //用于存储关注用户
    private List<PixelUser> pixelUserList = new ArrayList<>();
    //用于关注用户作品的复合查询
    private List<BmobQuery<BmobCanvas>> bmobQueryList = new ArrayList<>();
    //用于查询用户
    private BmobQuery<PixelUser> loadMoreUserQuery;
    //用于查询作品
    private BmobQuery<BmobCanvas> loadMoreCanvasQuery;
    private int pageLimit = 10;
    private int querySkip = 0;

    //适配器
    private FollowCanvasAdapter adapter;

    private View view;
    private SwipeRefreshLayout follwSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private LVBlazeWood lvBlazeWood;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_follow, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        refreshData();
    }

    private void initView() {
        recyclerView = view.findViewById(R.id.follow_recyclerview);
        follwSwipeRefreshLayout = view.findViewById(R.id.follow_swiperefreshlayout);
        lvBlazeWood = view.findViewById(R.id.follow_loadinganim);
        //设置布局管理器
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.getInstance()));
        //设置缓存
        recyclerView.setItemViewCacheSize(50);
        //设置子项布局大小不变，省去重新测量过程，提升性能
        recyclerView.setHasFixedSize(true);
        //设置适配器
        adapter = new FollowCanvasAdapter(bmobCanvasList);
        recyclerView.setAdapter(adapter);
        //添加滚动监听事件，用于上拉加载
        recyclerView.addOnScrollListener(new MainEndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                loadMoreData();
            }
        });
        //设置刷新监听事件，用于下拉刷新
        follwSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
    }

    private void refreshData() {
        PixelUser curPixelUser = BmobUser.getCurrentUser(PixelUser.class);
        loadingAnimStart();

        //初始化查询队列
        loadMoreUserQuery = new BmobQuery<>();
        loadMoreUserQuery.addWhereRelatedTo("followUsers", new BmobPointer(curPixelUser));
        loadMoreUserQuery.findObjects(new FindListener<PixelUser>() {
            @Override
            public void done(List<PixelUser> list, BmobException e) {
                if (e == null) {
                    //获得所有关注用户
                    pixelUserList.clear();
                    pixelUserList.addAll(list);

                    bmobQueryList.clear();
                    for (int i = 0; i < pixelUserList.size(); i++) {
                        BmobQuery<BmobCanvas> tmpBmobCanvasQuery = new BmobQuery<>();
                        tmpBmobCanvasQuery.addWhereEqualTo("creator", pixelUserList.get(i));
                        bmobQueryList.add(tmpBmobCanvasQuery);
                    }

                    //取当前时间对应的BmobDate用于数据查询
                    Date currentDate = new Date(System.currentTimeMillis());
                    BmobDate bmobCurrentDate = new BmobDate(currentDate);
                    //初始化查询队列
                    querySkip = 0;
                    loadMoreCanvasQuery = new BmobQuery<>();
                    loadMoreCanvasQuery.or(bmobQueryList);
                    loadMoreCanvasQuery.order("-createdAt");
                    loadMoreCanvasQuery.include("creator");
                    loadMoreCanvasQuery.addWhereLessThan("createdAt", bmobCurrentDate);
                    loadMoreCanvasQuery.setLimit(pageLimit);
                    loadMoreCanvasQuery.setSkip(0);
                    loadMoreCanvasQuery.findObjects(new FindListener<BmobCanvas>() {
                        @Override
                        public void done(List<BmobCanvas> list, BmobException e) {
                            if (e == null) {
                                if (list.isEmpty())
                                    adapter.setLoadState(adapter.LOADING_END);
                                else {
                                    //下拉刷新前先清空之前的数据
                                    bmobCanvasList.clear();
                                    //添加新数据
                                    bmobCanvasList.addAll(list);
                                    adapter.notifyDataSetChanged();
                                    adapter.setLoadState(adapter.LOADING_COMPLETE);
                                }
                                loadingAnimStop();
                            } else {
                                Toast.makeText(MainActivity.getInstance(), "作品获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.getInstance(), "关注用户获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMoreData() {
        //初始化查询队列
        querySkip += pageLimit;
        adapter.setLoadState(adapter.LOADING);
        loadMoreCanvasQuery.setSkip(querySkip);
        loadMoreCanvasQuery.findObjects(new FindListener<BmobCanvas>() {
            @Override
            public void done(List<BmobCanvas> list, BmobException e) {
                if (e == null) {
                    if (list.isEmpty())
                        adapter.setLoadState(adapter.LOADING_END);
                    else {
                        //上拉加载更多不需要清空之前的数据
                        //添加新数据
                        bmobCanvasList.addAll(list);
                        adapter.setLoadState(adapter.LOADING_COMPLETE);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(MainActivity.getInstance(), "数据获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //数据加载动画启动
    private void loadingAnimStart() {
        recyclerView.setVisibility(View.INVISIBLE);
        lvBlazeWood.setVisibility(View.VISIBLE);
        follwSwipeRefreshLayout.setRefreshing(false);
        lvBlazeWood.startAnim();
    }

    //数据加载动画停止
    private void loadingAnimStop() {
        lvBlazeWood.stopAnim();
        lvBlazeWood.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}
