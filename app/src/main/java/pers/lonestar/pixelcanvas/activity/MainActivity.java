package pers.lonestar.pixelcanvas.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.fragment.FollowFragment;
import pers.lonestar.pixelcanvas.fragment.WorldFragmnet;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.utils.BlurTransformation;
import pers.lonestar.pixelcanvas.utils.UpdateUtils;

public class MainActivity extends AppCompatActivity {
    private int REQUEST_CODE_PERMISSION = 1997;
    private static MainActivity instance;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ImageView avatar;
    private ImageView avatarBackground;
    private TextView nickName;
    private TextView introduction;
    private PixelUser pixelUser;
    private List<Fragment> fragmentList = new ArrayList<>();

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestUpdatePermissions();
        instance = this;

        initView();
        initListener();
        addFragment();
    }

    private void initView() {
        drawerLayout = findViewById(R.id.main_drawerlayout);
        navigationView = findViewById(R.id.nav_view);
        tabLayout = findViewById(R.id.main_tablayout);
        viewPager = findViewById(R.id.main_viewpager);
        toolbar = findViewById(R.id.main_activity_toolbar);
        toolbar.setTitle("像素画板");
        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void initListener() {
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull final View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                pixelUser = BmobUser.getCurrentUser(PixelUser.class);
                if (avatar == null) {
                    avatar = findViewById(R.id.nav_avatar);
                    avatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            PixelUser currentUser = BmobUser.getCurrentUser(PixelUser.class);
                            intent.putExtra("pixel_user", currentUser);
                            startActivity(intent);
                            drawerLayout.closeDrawers();
                        }
                    });
                }
                if (avatarBackground == null) {
                    avatarBackground = findViewById(R.id.nav_bg);
                }
                if (nickName == null) {
                    nickName = findViewById(R.id.nav_nickname);
                }
                if (introduction == null) {
                    introduction = findViewById(R.id.nav_introduction);
                }

                //设置昵称
                nickName.setText(pixelUser.getNickname());
                //设置个人简介
                introduction.setText(pixelUser.getIntroduction());
                //设置头像
                Glide.with(MainActivity.this)
                        .load(pixelUser.getAvatarUrl())
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(avatar);
                Glide.with(MainActivity.this)
                        .load(pixelUser.getAvatarUrl())
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(10, 1)))
                        .into(avatarBackground);
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
                    case R.id.nav_profile:
                        intent = new Intent(MainActivity.this, ProfileActivity.class);
                        //访问个人主页，传递PixelUser对象
                        PixelUser currentUser = BmobUser.getCurrentUser(PixelUser.class);
                        intent.putExtra("pixel_user", currentUser);
                        break;
                    case R.id.nav_follow:
                        intent = new Intent(MainActivity.this, FollowActivity.class);
                        break;
                    case R.id.nav_favorite:
                        intent = new Intent(MainActivity.this, FavoriteActivity.class);
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

    private void addFragment() {
        final String[] titles = new String[]{"世界", "关注"};
        for (String title : titles) {
            tabLayout.addTab(tabLayout.newTab().setText(title));
        }
        WorldFragmnet worldFragmnet = new WorldFragmnet();
        FollowFragment followFragment = new FollowFragment();
        fragmentList.add(worldFragmnet);
        fragmentList.add(followFragment);
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return titles[position];
            }
        });
        tabLayout.setupWithViewPager(viewPager);
    }


    private void requestUpdatePermissions() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意，执行操作
                UpdateUtils.checkUpdate(this);
            } else {
                //用户不同意，向用户展示该权限作用
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setMessage("应用需要读取和写入外部存储来进行软件更新，否则部分功能可能无法使用")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestUpdatePermissions();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .create()
                            .show();
                }
            }
        }
    }
}
