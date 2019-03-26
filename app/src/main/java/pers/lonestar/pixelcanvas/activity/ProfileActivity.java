package pers.lonestar.pixelcanvas.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private PixelUser pixelUser;
    private List<BmobCanvas> bmobCanvasList;
    private RecyclerView recyclerView;

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
        loadImg();
        initCanvasList();
    }

    private void loadData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        if (bmobCanvasList == null)
            Log.d("TEST", "实际数据为空");
        else
            Log.d("TEST", "实际数据长度：" + bmobCanvasList.size());
        BmobCanvasAdapter adapter = new BmobCanvasAdapter(bmobCanvasList);
        recyclerView.setAdapter(adapter);
    }

    private void initView() {
        backgroundImg = findViewById(R.id.profile_background_img);
        avatar = findViewById(R.id.profile_avatar);
        recyclerView = findViewById(R.id.profile_recyclerview);
    }

    private void loadImg() {
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
    }

    private void initCanvasList() {
        BmobQuery<BmobCanvas> query = new BmobQuery<>();
        query.addWhereEqualTo("creatorID", PixelApp.pixelUser.getObjectId());
        query.findObjects(new FindListener<BmobCanvas>() {
            @Override
            public void done(List<BmobCanvas> list, BmobException e) {
                if (e == null) {
                    bmobCanvasList = list;
                    Log.d("TEST", "数据获取成功，长度：" + bmobCanvasList.size());
                    loadData();
                    Toast.makeText(ProfileActivity.getInstance(), "数据获取成功", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("TEST", "数据获取失败");
                    Toast.makeText(ProfileActivity.getInstance(), "数据获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
