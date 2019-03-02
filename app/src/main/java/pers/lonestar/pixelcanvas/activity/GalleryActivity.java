package pers.lonestar.pixelcanvas.activity;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.dialog.NewCanvasDialogFragment;

public class GalleryActivity extends AppCompatActivity {
    private FloatingActionButton fab;
    private NewCanvasDialogFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        fab = findViewById(R.id.gallery_activity_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment = new NewCanvasDialogFragment();
                fragment.show(getSupportFragmentManager(), "NewCanvasDialog");
            }
        });
    }

}
