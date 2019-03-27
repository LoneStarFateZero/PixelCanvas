package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import cn.bmob.v3.BmobUser;
import de.hdodenhof.circleimageview.CircleImageView;
import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.infostore.PixelUser;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private CircleImageView avatar;
    private TextView nickName;
    private TextView introduction;
    private PixelUser pixelUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);

        initView();
        initListener();
    }

    private void initView() {
        pixelUser = BmobUser.getCurrentUser(PixelUser.class);
        PixelApp.pixelUser = pixelUser;
        drawerLayout = findViewById(R.id.main_drawerlayout);
        navigationView = findViewById(R.id.nav_view);
    }

    private void initListener() {
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull final View drawerView, float slideOffset) {
                if (avatar == null) {
                    avatar = findViewById(R.id.nav_avatar);
                    avatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            drawerLayout.closeDrawers();
                        }
                    });
                }
                if (nickName == null) {
                    nickName = findViewById(R.id.nav_nickname);
                    nickName.setText(pixelUser.getNickname());
                }
                if (introduction == null) {
                    introduction = findViewById(R.id.nav_introduction);
                    if (pixelUser.getIntroduction() != null)
                        introduction.setText(pixelUser.getIntroduction());
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (pixelUser.getAvatarUrl() == null) {
                    Glide.with(MainActivity.this)
                            .load(R.drawable.avatar)
                            .into(avatar);
                } else {
                    Glide.with(MainActivity.this)
                            .load(pixelUser.getAvatarUrl())
                            .into(avatar);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                switch (item.getItemId()) {
                    case R.id.nav_gallery:
                        intent = new Intent(MainActivity.this, GalleryActivity.class);
                        break;
                    case R.id.nav_publish:
                        intent = new Intent(MainActivity.this, PublishActivity.class);
                        break;
                    case R.id.nav_profile:
                        intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.putExtra("pixel_user", PixelApp.pixelUser);
                        break;
                    case R.id.nav_settings:
                        intent = new Intent(MainActivity.this, SettingsActivity.class);
                        break;
                }
                drawerLayout.closeDrawers();
                startActivity(intent);
                return true;
            }
        });
    }
}
