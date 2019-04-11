package pers.lonestar.pixelcanvas.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class PublishActivity extends AppCompatActivity {
    private LitePalCanvas litePalCanvas;
    private ImageView thumbnail;
    private EditText titleEditText;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        Intent intent = getIntent();
        litePalCanvas = (LitePalCanvas) intent.getSerializableExtra("local_canvas");

        initView();
        loadCanvas();
    }

    private void initView() {
        thumbnail = findViewById(R.id.post_thumbnaill);
        titleEditText = findViewById(R.id.post_title);
        toolbar = findViewById(R.id.post_toolbar);
        toolbar.setTitle("作品发布");
        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        //设置自定义Toolbar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void loadCanvas() {
        Glide.with(this).load(ParameterUtils.bytesToBitmap(litePalCanvas.getThumbnail())).into(thumbnail);
        titleEditText.setText(litePalCanvas.getCanvasName());
    }

    //发布对话框
    private void showPostDialog() {
        if (titleEditText.getText().toString().equals("")) {
            Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("确定要发布这个作品吗？");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postCanvasFile();
                //考虑发布作品之后转到个人主页
                finish();
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    //发布作品
    private void postCanvasFile() {
        BmobCanvas bmobCanvas = new BmobCanvas();
        bmobCanvas.setCanvasName(titleEditText.getText().toString());
        bmobCanvas.setCreator(BmobUser.getCurrentUser(PixelUser.class));
        bmobCanvas.setPixelCount(litePalCanvas.getPixelCount());
        bmobCanvas.setJsonData(litePalCanvas.getJsonData());
        bmobCanvas.setThumbnail(litePalCanvas.getThumbnail());

        bmobCanvas.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    Toast.makeText(GalleryActivity.getInstance(), "作品发布成功！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GalleryActivity.getInstance(), "作品发布失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.post_canvas:
                showPostDialog();
                break;
        }
        return true;
    }
}
