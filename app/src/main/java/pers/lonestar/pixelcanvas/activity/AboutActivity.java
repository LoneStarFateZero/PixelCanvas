package pers.lonestar.pixelcanvas.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;
import pers.lonestar.pixelcanvas.R;

public class AboutActivity extends BaseSwipeBackActivity {
    private LinearLayout linearLayout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        linearLayout = (LinearLayout) findViewById(R.id.about_linearlayout);
        toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        toolbar.setTitle("关于");
        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.ic_launcher)
                .setDescription("一个简单的像素绘图和分享APP，你可以通过拖动画笔绘制一个简单的像素艺术，并和大家分享你的像素艺术。")
                .addItem(new Element().setTitle("Version 1.0"))
                .addGroup("与我联系")
                .addEmail("18815755562@163.com", "与我联系")
                .addWebsite("https://github.com/LoneStarFateZero/PixelCanvas", "提点意见")
                .addGitHub("LoneStarFateZero", "关注 Github")
                .create();
        linearLayout.addView(aboutPage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
