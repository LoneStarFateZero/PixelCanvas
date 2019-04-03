package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cn.bmob.v3.BmobUser;
import de.hdodenhof.circleimageview.CircleImageView;
import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.fragment.FollowFragment;
import pers.lonestar.pixelcanvas.fragment.WorldFragmnet;
import pers.lonestar.pixelcanvas.infostore.PixelUser;

public class MainActivity extends AppCompatActivity {
    private static MainActivity instance;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private CircleImageView avatar;
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
        instance = this;

        toolbar = findViewById(R.id.main_activity_toolbar);
        toolbar.setTitle("像素画板");
        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        setSupportActionBar(toolbar);

        initView();
        initListener();
        addFragment();
    }

    private void initView() {
        pixelUser = BmobUser.getCurrentUser(PixelUser.class);
        PixelApp.pixelUser = pixelUser;
        drawerLayout = findViewById(R.id.main_drawerlayout);
        navigationView = findViewById(R.id.nav_view);
        tabLayout = findViewById(R.id.main_tablayout);
        viewPager = findViewById(R.id.main_viewpager);
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
                            intent.putExtra("pixel_user", PixelApp.pixelUser);
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
                //设置头像
                Glide.with(MainActivity.this)
                        .load(pixelUser.getAvatarUrl())
                        .into(avatar);
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
                        //访问个人主页，传递PixelUser对象
                        intent.putExtra("pixel_user", PixelApp.pixelUser);
                        break;
                    case R.id.nav_follow:
                        intent = new Intent(MainActivity.this, FollowActivity.class);
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

    //TODO
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
}
