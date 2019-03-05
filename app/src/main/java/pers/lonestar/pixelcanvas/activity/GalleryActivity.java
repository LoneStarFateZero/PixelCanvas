package pers.lonestar.pixelcanvas.activity;

import android.os.Bundle;
import android.view.View;

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


    @Override
    protected void onStart() {
        super.onStart();
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
        litePalCanvasList = LitePal.findAll(LitePalCanvas.class);
    }
}
