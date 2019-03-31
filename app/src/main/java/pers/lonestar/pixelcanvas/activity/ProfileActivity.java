package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ldoublem.loadingviewlib.view.LVBlazeWood;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
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
    private ImageView backgroundImg;
    private CircleImageView avatar;
    private Toolbar toolbar;
    private TextView userNickName;
    private FloatingActionButton editFab;
    private PixelUser pixelUser;
    private List<BmobCanvas> bmobCanvasList;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BmobQuery<BmobCanvas> loadMoreQuery;
    private ProfileCanvasAdapter adapter;
    private LVBlazeWood lvBlazeWood;

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
        initListener();
        loadInfo();
        initCanvasList();
    }

    //相关设置
    private void initListener() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
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
        recyclerView.setVisibility(View.GONE);
    }

    //加载数据
    private void loadData() {
        bmobCanvasList = new ArrayList<>();
        adapter = new ProfileCanvasAdapter(bmobCanvasList);
        recyclerView.setAdapter(adapter);
        //设置缓存
        recyclerView.setItemViewCacheSize(50);
        //设置子项布局大小不变，省去重新测量过程，提升性能
        recyclerView.setHasFixedSize(true);
    }

    //初始化View
    private void initView() {
        backgroundImg = findViewById(R.id.profile_background_img);
        avatar = findViewById(R.id.profile_avatar);
        editFab = findViewById(R.id.profile_fab);
        //如果访问的是他人的主页，则不显示编辑按钮，只有自己的主页才可以编辑
        if (!PixelApp.pixelUser.getObjectId().equals(pixelUser.getObjectId()))
            editFab.setVisibility(View.GONE);
        toolbar = findViewById(R.id.profile_toolbar);
        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        userNickName = findViewById(R.id.profile_nickname);
        swipeRefreshLayout = findViewById(R.id.profile_swipe);
        lvBlazeWood = findViewById(R.id.profile_loadinganim);
        recyclerView = findViewById(R.id.profile_recyclerview);
    }

    //加载头像和背景
    private void loadInfo() {
        if (pixelUser.getAvatarUrl() == null) {
            Glide.with(this)
                    .load(PixelApp.defaultAvatarUrl)
                    .into(avatar);
            Glide.with(this)
//                    .load(R.drawable.avatar)
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
        userNickName.setText(pixelUser.getNickname());
    }

    //初始化载入数据
    private void initCanvasList() {
        loadingAnimStart();
        //更新用户信息
        BmobUser.fetchUserInfo(new FetchUserInfoListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if (e == null) {
                    pixelUser = BmobUser.getCurrentUser(PixelUser.class);
                    loadInfo();
                } else {
                    Toast.makeText(ProfileActivity.getInstance(), "数据获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadData();

        //初始化加载更多查询，用于上拉加载更多
        loadMoreQuery = new BmobQuery<>();
        loadMoreQuery.addWhereEqualTo("creatorID", pixelUser.getObjectId());
        loadMoreQuery.order("-createdAt");
        loadMoreQuery.setLimit(10);
        loadMoreQuery.setSkip(0);

        adapter.setLoadState(adapter.LOADING);

        loadMoreQuery.findObjects(new FindListener<BmobCanvas>() {
            @Override
            public void done(List<BmobCanvas> list, BmobException e) {
                if (e == null) {
                    loadingAnimStop();
                    if (list.isEmpty())
                        adapter.setLoadState(adapter.LOADING_END);
                    else {
                        String createdAt = list.get(list.size() - 1).getCreatedAt();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date createdAtDate = null;
                        try {
                            createdAtDate = sdf.parse(createdAt);
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        BmobDate bmobCreatedAtDate = new BmobDate(createdAtDate);
                        loadMoreQuery.addWhereLessThan("createdAt", bmobCreatedAtDate);
                        //不可以直接bmobCanvasList=list
                        //会导致adapter中的观察者观察的是原来的bmobCanvasList，而不是新bmobCanvasList
                        bmobCanvasList.clear();
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

    //下拉刷新
    private void refreshData() {
        loadingAnimStart();
        //更新用户信息
        BmobUser.fetchUserInfo(new FetchUserInfoListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if (e == null) {
                    pixelUser = BmobUser.getCurrentUser(PixelUser.class);
                    loadInfo();
                } else {
                    Toast.makeText(ProfileActivity.getInstance(), "数据获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //下拉刷新后需要重新初始化加载更多查询
        loadMoreQuery = new BmobQuery<>();
        loadMoreQuery.addWhereEqualTo("creatorID", pixelUser.getObjectId());
        loadMoreQuery.order("-createdAt");
        loadMoreQuery.setLimit(10);
        loadMoreQuery.setSkip(0);

        adapter.setLoadState(adapter.LOADING);

        //更新作品信息
        loadMoreQuery.findObjects(new FindListener<BmobCanvas>() {
            @Override
            public void done(List<BmobCanvas> list, BmobException e) {
                if (e == null) {
                    loadingAnimStop();
                    bmobCanvasList.clear();
                    bmobCanvasList.addAll(list);
                    swipeRefreshLayout.setRefreshing(false);
                    if (list.isEmpty())
                        adapter.setLoadState(adapter.LOADING_END);
                    else {
                        String createdAt = list.get(list.size() - 1).getCreatedAt();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date createdAtDate = null;
                        try {
                            createdAtDate = sdf.parse(createdAt);
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        BmobDate bmobCreatedAtDate = new BmobDate(createdAtDate);
                        loadMoreQuery.addWhereLessThan("createdAt", bmobCreatedAtDate);
                        adapter.setLoadState(adapter.LOADING_COMPLETE);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(ProfileActivity.getInstance(), "数据获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
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
                        String createdAt = list.get(list.size() - 1).getCreatedAt();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date createdAtDate = null;
                        try {
                            createdAtDate = sdf.parse(createdAt);
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        BmobDate bmobCreatedAtDate = new BmobDate(createdAtDate);
                        loadMoreQuery.addWhereLessThan("createdAt", bmobCreatedAtDate);
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
}
