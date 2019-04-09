package pers.lonestar.pixelcanvas.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.adapter.LocalCanvasAdapter;
import pers.lonestar.pixelcanvas.dialog.NewCanvasDialogFragment;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;

public class GalleryActivity extends BaseSwipeBackActivity {
    private FloatingActionButton fab;
    private NewCanvasDialogFragment fragment;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private List<LitePalCanvas> litePalCanvasList;
    private LocalCanvasAdapter localCanvasAdapter;
    private static GalleryActivity instance;

    public static GalleryActivity getInstance() {
        return instance;
    }

    //onStart()似乎不会在PaintActivity返回时被调用，此处采用onResume()方法来刷新数据
    @Override
    protected void onResume() {
        super.onResume();
        initCanvasList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        instance = this;

        initView();
        initListener();
    }

    private void initListener() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment = new NewCanvasDialogFragment();
                fragment.show(getSupportFragmentManager(), "NewCanvasDialog");
            }
        });

        litePalCanvasList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        localCanvasAdapter = new LocalCanvasAdapter(litePalCanvasList);
        recyclerView.setAdapter(localCanvasAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        fab.show();
                        Glide.with(GalleryActivity.this).resumeRequests();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        Glide.with(GalleryActivity.this).pauseRequests();
                        fab.hide();
                        break;
                }
            }
        });
    }

    private void initView() {
        fab = (FloatingActionButton) findViewById(R.id.gallery_activity_fab);
        recyclerView = (RecyclerView) findViewById(R.id.gallery_activity_recyclerview);
        toolbar = (Toolbar) findViewById(R.id.gallery_activity_toolbar);
        toolbar.setTitle("本地作品");
        //设置标题字体样式为像素字体，否则为默认字体，与整体像素风格不匹配
        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void initCanvasList() {
        litePalCanvasList.clear();
        litePalCanvasList.addAll(LitePal.order("id desc").find(LitePalCanvas.class));
        localCanvasAdapter.notifyDataSetChanged();
//        litePalCanvasList = LitePal.order("id desc").find(LitePalCanvas.class);
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

    @Override
    public void onBackPressed() {
        finish();
    }
}
