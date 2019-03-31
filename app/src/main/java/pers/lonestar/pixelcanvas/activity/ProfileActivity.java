package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ldoublem.loadingviewlib.view.LVBlazeWood;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;
import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.adapter.ProfileCanvasAdapter;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.listener.EndlessRecyclerOnScrollListener;
import pers.lonestar.pixelcanvas.utils.BlurTransformation;

public class ProfileActivity extends AppCompatActivity {
    private static ProfileActivity instance;
    private Toolbar toolbar;
    //背景
    private ImageView backgroundImg;
    //头像
    private CircleImageView avatar;
    //昵称
    private TextView userNickName;
    //信息编辑浮动按钮
    private FloatingActionButton editFab;

    private RecyclerView recyclerView;
    //RecyclerView适配器
    private ProfileCanvasAdapter adapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    //loading篝火动画View组件
    private LVBlazeWood lvBlazeWood;

    //个人主页用户
    private PixelUser pixelUser;
    //作品数据
    private List<BmobCanvas> bmobCanvasList;
    //Bmob数据查询
    private BmobQuery<BmobCanvas> loadMoreQuery;
    private int querySkip;
    private int queryLimit = 5;

    public static ProfileActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        instance = this;
        Intent intent = getIntent();
        pixelUser = (PixelUser) intent.getSerializableExtra("pixel_user");
        initView();
        loadInfo();
        refreshData();
    }

    //初始化View
    private void initView() {
        backgroundImg = findViewById(R.id.profile_background_img);
        avatar = findViewById(R.id.profile_avatar);
        editFab = findViewById(R.id.profile_fab);
        toolbar = findViewById(R.id.profile_toolbar);
        userNickName = findViewById(R.id.profile_nickname);
        swipeRefreshLayout = findViewById(R.id.profile_swipe);
        lvBlazeWood = findViewById(R.id.profile_loadinganim);
        recyclerView = findViewById(R.id.profile_recyclerview);

        //如果访问的是他人的主页，则不显示编辑按钮，只有自己的主页才可以编辑
        if (!PixelApp.pixelUser.getObjectId().equals(pixelUser.getObjectId()))
            editFab.setVisibility(View.GONE);
        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        //设置线性布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //设置适配器
        bmobCanvasList = new ArrayList<>();
        adapter = new ProfileCanvasAdapter(bmobCanvasList);
        recyclerView.setAdapter(adapter);
        //设置缓存
        recyclerView.setItemViewCacheSize(50);
        //设置子项布局大小不变，省去重新测量过程，提升性能
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                //获取更多数据
                loadMoreData();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新RecyclerView数据
                refreshData();
            }
        });
    }

    //下拉刷新
    private void refreshData() {
        loadingAnimStart();
        //取当前时间对应的BmobDate用于数据查询
        Date currentDate = new Date(System.currentTimeMillis());
        BmobDate bmobCurrentDate = new BmobDate(currentDate);

        //下拉刷新后需要重新初始化加载更多查询
        querySkip = 0;
        loadMoreQuery = new BmobQuery<>();
        loadMoreQuery.addWhereEqualTo("creatorID", pixelUser.getObjectId());
        loadMoreQuery.addWhereLessThan("createdAt", bmobCurrentDate);
        loadMoreQuery.order("-createdAt");
        loadMoreQuery.setLimit(queryLimit);
        loadMoreQuery.setSkip(0);

        //更新作品信息
        loadMoreQuery.findObjects(new FindListener<BmobCanvas>() {
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
                    Toast.makeText(ProfileActivity.getInstance(), "作品获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //上拉加载
    private void loadMoreData() {
        querySkip += queryLimit;
        loadMoreQuery.setSkip(querySkip);
        adapter.setLoadState(adapter.LOADING);
        loadMoreQuery.findObjects(new FindListener<BmobCanvas>() {
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
                    Toast.makeText(ProfileActivity.getInstance(), "数据获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //加载头像、背景和昵称
    private void loadInfo() {
        if (pixelUser.getAvatarUrl() == null) {
            Glide.with(this)
                    .load(PixelApp.defaultAvatarUrl)
                    .into(avatar);
            Glide.with(this)
                    .load(PixelApp.defaultAvatarUrl)
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(15, 1)))
                    .into(backgroundImg);
        } else {
            Glide.with(this)
                    .load(pixelUser.getAvatarUrl())
                    .into(avatar);
            Glide.with(this)
                    .load(pixelUser.getAvatarUrl())
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(15, 1)))
                    .into(backgroundImg);
        }
        Log.d("TEST", "用户昵称：" + pixelUser.getNickname());
        userNickName.setText(pixelUser.getNickname());
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
