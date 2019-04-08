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
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;
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
    //用户关注浮动按钮
    private FloatingActionButton followFab;

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
    private int pageLimit = 5;
    private Boolean isFollowing;

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
        editFab = findViewById(R.id.profile_edit_fab);
        followFab = findViewById(R.id.profile_follow_fab);
        toolbar = findViewById(R.id.profile_toolbar);
        userNickName = findViewById(R.id.profile_nickname);
        swipeRefreshLayout = findViewById(R.id.profile_swipe);
        lvBlazeWood = findViewById(R.id.profile_loadinganim);
        recyclerView = findViewById(R.id.profile_recyclerview);

        //如果访问的是他人的主页，则不显示编辑按钮，只有自己的主页才可以编辑
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
        //开启加载动画
        loadingAnimStart();

        //更新当前主页用户信息
        BmobQuery<PixelUser> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("objectId", pixelUser.getObjectId());
        bmobQuery.findObjects(new FindListener<PixelUser>() {
            @Override
            public void done(List<PixelUser> list, BmobException e) {
                if (e == null) {
                    PixelUser newPixelUser = list.get(0);
                    pixelUser.setNickname(newPixelUser.getNickname());
                    pixelUser.setIntroduction(newPixelUser.getIntroduction());
                    pixelUser.setAvatarUrl(newPixelUser.getAvatarUrl());
                    loadInfo();
                }
            }
        });

        //取当前时间对应的BmobDate用于数据查询
        Date currentDate = new Date(System.currentTimeMillis());
        BmobDate bmobCurrentDate = new BmobDate(currentDate);

        //下拉刷新后需要重新初始化加载更多查询
        querySkip = 0;
        loadMoreQuery = new BmobQuery<>();
        loadMoreQuery.addWhereEqualTo("creator", pixelUser);
        loadMoreQuery.addWhereLessThan("createdAt", bmobCurrentDate);
        loadMoreQuery.order("-createdAt");
        loadMoreQuery.setLimit(pageLimit);
        loadMoreQuery.setSkip(0);

        //更新作品信息
        loadMoreQuery.findObjects(new FindListener<BmobCanvas>() {
            @Override
            public void done(List<BmobCanvas> list, BmobException e) {
                if (e == null) {
                    //下拉刷新前先清空之前的数据
                    bmobCanvasList.clear();
                    //添加新数据
                    bmobCanvasList.addAll(list);
                    adapter.notifyDataSetChanged();
                    if (list.isEmpty())
                        adapter.setLoadState(adapter.LOADING_END);
                    else {
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
        querySkip += pageLimit;
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

    //加载头像、背景和昵称，以及关注状态
    private void loadInfo() {
        Glide.with(this)
                .load(pixelUser.getAvatarUrl())
                .into(avatar);
        Glide.with(this)
                .load(pixelUser.getAvatarUrl())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(15, 1)))
                .into(backgroundImg);
        userNickName.setText(pixelUser.getNickname());

        //关注状态查询
        final PixelUser curPixelUser = BmobUser.getCurrentUser(PixelUser.class);
        //不是本人主页
        if (!curPixelUser.getObjectId().equals(pixelUser.getObjectId())) {
            editFab.setVisibility(View.GONE);
            followFab.setVisibility(View.VISIBLE);
            //查询是否已经关注
            final BmobQuery<PixelUser> bmobQuery = new BmobQuery<>();
            bmobQuery.addWhereRelatedTo("followUsers", new BmobPointer(curPixelUser));
            bmobQuery.addQueryKeys("objectId");
            bmobQuery.findObjects(new FindListener<PixelUser>() {
                @Override
                public void done(List<PixelUser> list, BmobException e) {
                    if (e == null) {
                        for (int i = 0; i < list.size(); i++) {
                            if (pixelUser.getObjectId().equals(list.get(i).getObjectId())) {
                                //已关注
                                isFollowing = true;
                                followFab.setImageResource(R.drawable.ic_following);
                                return;
                            }
                        }
                        //未关注
                        isFollowing = false;
                        followFab.setImageResource(R.drawable.ic_not_following);
                    }
                }
            });
            followFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //已关注则取消关注
                    if (isFollowing != null && isFollowing) {
                        BmobRelation bmobRelation = new BmobRelation();
                        bmobRelation.remove(pixelUser);
                        curPixelUser.setFollowUsers(bmobRelation);
                        curPixelUser.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    isFollowing = false;
                                    followFab.setImageResource(R.drawable.ic_not_following);
                                    Toast.makeText(ProfileActivity.this, "已取消关注", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(ProfileActivity.this, "操作失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    //未关注则进行关注
                    else if (isFollowing != null && !isFollowing) {
                        PixelUser curUser = new PixelUser();
                        curUser.setObjectId(BmobUser.getCurrentUser(PixelUser.class).getObjectId());
                        PixelUser followUser = new PixelUser();
                        followUser.setObjectId(pixelUser.getObjectId());
                        BmobRelation followRelation = new BmobRelation();
                        followRelation.add(followUser);
                        curUser.setFollowUsers(followRelation);
                        curUser.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    isFollowing = true;
                                    followFab.setImageResource(R.drawable.ic_following);
                                    Toast.makeText(ProfileActivity.this, "关注成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ProfileActivity.this, "操作失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        } else {
            followFab.setVisibility(View.GONE);
            editFab.setVisibility(View.VISIBLE);
            editFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //转到个人信息编辑页面
                    Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                    startActivity(intent);
                }
            });
        }
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
