package pers.lonestar.pixelcanvas.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import cn.bmob.v3.BmobUser;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.utils.glide.GlideCatchUtil;

public class SettingsActivity extends BaseSwipeBackActivity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private Toolbar toolbar;
    private RelativeLayout autoUpdate;
    private Switch autoUpdateSwitch;
    private LinearLayout cleanCatch;
    private LinearLayout about;
    private LinearLayout appInfo;
    private LinearLayout exitCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);

        initView();
        initListener();
    }

    private void initView() {
        autoUpdate = (RelativeLayout) findViewById(R.id.settings_autoupdate);
        autoUpdateSwitch = (Switch) findViewById(R.id.settings_autoupdate_switch);
        cleanCatch = (LinearLayout) findViewById(R.id.settings_cleancatch);
        about = (LinearLayout) findViewById(R.id.settings_about);
        appInfo = (LinearLayout) findViewById(R.id.settings_appinfo);
        exitCurrentUser = (LinearLayout) findViewById(R.id.settings_exit);
        toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbar.setTitle("设置");
        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void initListener() {
        boolean autoUpdateFlag = sharedPreferences.getBoolean("auto_update", true);
        autoUpdateSwitch.setChecked(autoUpdateFlag);
        autoUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
                //是否自动更新
                editor.putBoolean("auto_update", !autoUpdateSwitch.isChecked());
                editor.apply();
                autoUpdateSwitch.setChecked(!autoUpdateSwitch.isChecked());
            }
        });

        cleanCatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GlideCatchUtil glideCatchUtil = GlideCatchUtil.getInstance();
                if (glideCatchUtil.getCacheSize().equals("0.0Byte")) {
                    Toast.makeText(SettingsActivity.this, "没有缓存需要清理", Toast.LENGTH_SHORT).show();
                    return;
                }
                //显示清理对话框，包括缓存大小
                new AlertDialog.Builder(SettingsActivity.this)
                        .setMessage("确定要清理掉 " + glideCatchUtil.getCacheSize() + " 图片缓存吗？")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //清理缓存
                                if (glideCatchUtil.clearCacheDiskSelf())
                                    Toast.makeText(SettingsActivity.this, "缓存清理完成", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(SettingsActivity.this, "缓存清理失败", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        appInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });

        exitCurrentUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //退出对话框
                new AlertDialog.Builder(SettingsActivity.this)
                        .setMessage("您确定要退出当前帐号吗？")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            //用户退出当前帐号
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BmobUser.logOut();
                                finish();
                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        }).setNegativeButton("取消", null)
                        .create()
                        .show();
            }
        });
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
