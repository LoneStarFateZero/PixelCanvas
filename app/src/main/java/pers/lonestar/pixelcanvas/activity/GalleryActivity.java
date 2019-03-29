package pers.lonestar.pixelcanvas.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.litepal.LitePal;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.adapter.CanvasAdapter;
import pers.lonestar.pixelcanvas.dialog.NewCanvasDialogFragment;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;

public class GalleryActivity extends AppCompatActivity {
    private FloatingActionButton fab;
    private NewCanvasDialogFragment fragment;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private List<LitePalCanvas> litePalCanvasList;
    private static GalleryActivity instance;

    public static GalleryActivity getInstance() {
        return instance;
    }

    @Override
    protected void onStart() {
        super.onStart();
        initCanvasList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        final CanvasAdapter canvasAdapter = new CanvasAdapter(litePalCanvasList);
        recyclerView.setAdapter(canvasAdapter);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        instance = this;

        initView();
        initListener();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void initListener() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment = new NewCanvasDialogFragment();
                fragment.show(getSupportFragmentManager(), "NewCanvasDialog");
            }
        });
    }

    private void initView() {
        toolbar = findViewById(R.id.gallery_activity_toolbar);
        fab = findViewById(R.id.gallery_activity_fab);
        recyclerView = findViewById(R.id.gallery_activity_recyclerview);
    }

    private void initCanvasList() {
        litePalCanvasList = LitePal.order("id desc").find(LitePalCanvas.class);
    }
}
