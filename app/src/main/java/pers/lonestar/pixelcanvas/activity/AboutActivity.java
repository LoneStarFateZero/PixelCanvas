package pers.lonestar.pixelcanvas.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;
import pers.lonestar.pixelcanvas.R;

public class AboutActivity extends BaseSwipeBackActivity {
    private int REQUEST_CODE_PERMISSION = 1997;
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
                .addItem(new Element().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestMatissePermissions();
                    }
                }).setTitle("当前版本 1.0.9"))
                .addGroup("与我联系")
                .addEmail("18815755562@163.com", "与我联系")
                .addWebsite("https://github.com/LoneStarFateZero/PixelCanvas", "提点意见")
                .addGitHub("LoneStarFateZero", "关注Github")
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

    private void requestMatissePermissions() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意，执行操作
                BmobUpdateAgent.forceUpdate(this);
                //更新监听
                BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {
                    @Override
                    public void onUpdateReturned(int statusCode, UpdateResponse updateResponse) {
                        //statusCode: 1无新版本 0有新版本 -1出错
                        if (statusCode == 1)
                            Toast.makeText(AboutActivity.this, "暂时没有检测到新版本", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                //用户不同意，向用户展示该权限作用
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setMessage("应用需要读取和写入外部存储来选择图片、导出图片和更新，否则部分功能可能无法使用")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestMatissePermissions();
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
