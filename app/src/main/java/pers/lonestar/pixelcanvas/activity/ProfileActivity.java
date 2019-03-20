package pers.lonestar.pixelcanvas.activity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.utils.BlurTransformation;

public class ProfileActivity extends AppCompatActivity {
    private ImageView backgroundImg;
    private CircleImageView avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initView();
        loadImg();
    }

    private void initView() {
        backgroundImg = findViewById(R.id.profile_background_img);
        avatar = findViewById(R.id.profile_avatar);
    }

    private void loadImg() {
        Glide.with(this)
                .load(getDrawable(R.drawable.avatar))
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(15, 1)))
                .into(backgroundImg);
    }
}
