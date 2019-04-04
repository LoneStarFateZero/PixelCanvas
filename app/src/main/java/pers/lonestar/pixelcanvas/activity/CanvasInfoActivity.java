package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class CanvasInfoActivity extends AppCompatActivity {
    private BmobCanvas pixelCanvas;
    private PixelUser pixelUser;
    private ImageView thumbnail;
    private CircleImageView avatar;
    private TextView nickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas_info);

        Intent intent = getIntent();
        pixelCanvas = (BmobCanvas) intent.getSerializableExtra("pixel_canvas");
        pixelUser = (PixelUser) intent.getSerializableExtra("pixel_user");

        loadCanvasInfo();
    }

    private void loadCanvasInfo() {
        thumbnail = findViewById(R.id.canvas_info_thumbnail);
        avatar = findViewById(R.id.canvas_info_avatar);
        nickName = findViewById(R.id.canvas_info_nickname);

        Glide.with(this).load(ParameterUtils.bytesToBitmap(pixelCanvas.getThumbnail())).into(thumbnail);
        Glide.with(this).load(pixelUser.getAvatarUrl()).into(avatar);
        nickName.setText(pixelUser.getNickname());
    }
}
