package pers.lonestar.pixelcanvas.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.adapter.FollowUserAdapter;
import pers.lonestar.pixelcanvas.infostore.PixelUser;

public class FollowActivity extends BaseSwipeBackActivity {
    private static FollowActivity instance;
    private RecyclerView recyclerView;
    private FollowUserAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private PixelUser pixelUser;
    private List<PixelUser> pixelUserList;

    public static FollowActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
        instance = this;

        initView();
        refreshData();
    }

    private void initView() {
        pixelUser = BmobUser.getCurrentUser(PixelUser.class);

        toolbar = (Toolbar) findViewById(R.id.follow_activity_toolbar);
        toolbar.setTitle("关注列表");
        //设置标题字体样式为像素字体，否则为默认字体，与整体像素风格不匹配
        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.follow_activity_SwipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.follow_activity_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pixelUserList = new ArrayList<>();
        adapter = new FollowUserAdapter(pixelUserList);
        recyclerView.setAdapter(adapter);
    }

    private void refreshData() {
        BmobQuery<PixelUser> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereRelatedTo("followUsers", new BmobPointer(pixelUser));
        bmobQuery.findObjects(new FindListener<PixelUser>() {
            @Override
            public void done(List<PixelUser> list, BmobException e) {
                swipeRefreshLayout.setRefreshing(false);
                if (e == null) {
                    pixelUserList.clear();
                    pixelUserList.addAll(list);
                    adapter.notifyDataSetChanged();
                } else
                    Toast.makeText(FollowActivity.this, "关注用户获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
            }
        });
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
