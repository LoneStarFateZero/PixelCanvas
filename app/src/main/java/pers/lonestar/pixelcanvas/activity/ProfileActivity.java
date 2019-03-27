package pers.lonestar.pixelcanvas.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ldoublem.loadingviewlib.view.LVBlazeWood;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;
import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.adapter.BmobCanvasAdapter;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.utils.BlurTransformation;

public class ProfileActivity extends AppCompatActivity {
    private static ProfileActivity instance;
    private ImageView backgroundImg;
    private CircleImageView avatar;
    private TextView userNickName;
    private PixelUser pixelUser;
    private List<BmobCanvas> bmobCanvasList;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BmobQuery<BmobCanvas> query;
    private BmobCanvasAdapter adapter;
    private LVBlazeWood lvBlazeWood;

    public static ProfileActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        instance = this;
        pixelUser = PixelApp.pixelUser;
        initView();
        initListener();
        loadInfo();
        initCanvasList();
    }

    //相关设置
    private void initListener() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
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
        adapter = new BmobCanvasAdapter(bmobCanvasList);
        recyclerView.setAdapter(adapter);
    }

    //初始化View
    private void initView() {
        backgroundImg = findViewById(R.id.profile_background_img);
        avatar = findViewById(R.id.profile_avatar);
        userNickName = findViewById(R.id.profile_nickname);
        swipeRefreshLayout = findViewById(R.id.profile_swipe);
        lvBlazeWood = findViewById(R.id.profile_loadinganim);
        recyclerView = findViewById(R.id.profile_recyclerview);
    }

    //加载头像和背景
    private void loadInfo() {
        if (pixelUser.getAvatarUrl() == null) {
            Glide.with(this)
                    .load(R.drawable.avatar)
                    .into(avatar);
            Glide.with(this)
                    .load(R.drawable.avatar)
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
        query = new BmobQuery<>();
        query.addWhereEqualTo("creatorID", PixelApp.pixelUser.getObjectId());
        query.findObjects(new FindListener<BmobCanvas>() {
            @Override
            public void done(List<BmobCanvas> list, BmobException e) {
                if (e == null) {
                    loadingAnimStop();
                    bmobCanvasList = list;
                    loadData();
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    Toast.makeText(ProfileActivity.getInstance(), "数据获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //下拉刷新
    private void refreshData() {
        loadingAnimStart();
        query.findObjects(new FindListener<BmobCanvas>() {
            @Override
            public void done(List<BmobCanvas> list, BmobException e) {
                if (e == null) {
                    loadingAnimStop();
                    bmobCanvasList = list;
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
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
        lvBlazeWood.startAnim();
    }

    //数据加载动画停止
    private void loadingAnimStop() {
        lvBlazeWood.stopAnim();
        lvBlazeWood.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}
