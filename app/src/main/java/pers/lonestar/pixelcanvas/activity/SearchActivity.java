package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.fragment.SearchCanvasFragment;
import pers.lonestar.pixelcanvas.fragment.SearchUserFragment;

public class SearchActivity extends BaseSwipeBackActivity {
    private static SearchActivity instance;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private List<Fragment> fragmentList = new ArrayList<>();
    private String queryData;

    public static SearchActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        instance = this;
        Intent intent = getIntent();
        queryData = intent.getStringExtra("query_data");

        initView();
        addFragment();
    }

    private void initView() {
        tabLayout = (TabLayout) findViewById(R.id.search_tablayout);
        viewPager = (ViewPager) findViewById(R.id.search_viewpager);
        toolbar = (Toolbar) findViewById(R.id.search_activity_toolbar);
        toolbar.setTitle("搜索结果");
        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void addFragment() {
        final String[] titles = new String[]{"作品", "用户"};
        for (String title : titles) {
            tabLayout.addTab(tabLayout.newTab().setText(title));
        }
        SearchCanvasFragment searchCanvasFragment = new SearchCanvasFragment();
        searchCanvasFragment.setQueryCanvasName(queryData);
        SearchUserFragment searchUserFragment = new SearchUserFragment();
        searchUserFragment.setQueryCanvasName(queryData);
        fragmentList.add(searchCanvasFragment);
        fragmentList.add(searchUserFragment);
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

            @NotNull
            @Override
            public CharSequence getPageTitle(int position) {
                return titles[position];
            }
        });
        tabLayout.setupWithViewPager(viewPager);
    }
}
