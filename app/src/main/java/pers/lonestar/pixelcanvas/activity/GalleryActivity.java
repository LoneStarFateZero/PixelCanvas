package pers.lonestar.pixelcanvas.activity;

import android.os.Bundle;
import android.view.View;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.litepal.LitePal;

import java.util.List;

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
        YoYo.with(Techniques.ZoomIn).duration(400).playOn(fab);
        initCanvasList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        CanvasAdapter canvasAdapter = new CanvasAdapter(litePalCanvasList);
        recyclerView.setAdapter(canvasAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        instance = this;

        toolbar = findViewById(R.id.gallery_activity_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

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
    }

    private void initView() {
        fab = findViewById(R.id.gallery_activity_fab);
        recyclerView = findViewById(R.id.gallery_activity_recyclerview);
    }

    private void initCanvasList() {
        litePalCanvasList = LitePal.order("id desc").find(LitePalCanvas.class);
    }
}
