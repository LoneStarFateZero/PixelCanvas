package pers.lonestar.pixelcanvas.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import de.hdodenhof.circleimageview.CircleImageView;
import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.infostore.PixelUser;

public class ProfileEditActivity extends AppCompatActivity {
    private boolean changeFlag = false;
    private boolean avatarChangeFlag = false;
    private int REQUEST_CODE_IMAGE = 9527;
    private int REQUEST_CODE_PERMISSION = 1997;
    private CircleImageView avatar;
    private EditText nickNameEdit;
    private EditText introductionEdit;
    private TextView nickName;
    private TextView introduction;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private PixelUser currentUser;
    private Uri avatarUri;
    private String avatarPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        initView();
    }

    private void initView() {
        avatar = findViewById(R.id.profile_edit_avatar);
        nickName = findViewById(R.id.profile_edit_nickname);
        nickNameEdit = findViewById(R.id.profile_edit_nickname_edit);
        introduction = findViewById(R.id.profile_edit_intro);
        introductionEdit = findViewById(R.id.profile_edit_intro_edit);
        fab = findViewById(R.id.profile_edit_fab);
        toolbar = findViewById(R.id.profile_edit_toolbar);

        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        currentUser = BmobUser.getCurrentUser(PixelUser.class);
        nickName.setText(currentUser.getNickname());
        nickNameEdit.setText(currentUser.getNickname());
        introduction.setText(currentUser.getIntroduction());
        introductionEdit.setText(currentUser.getIntroduction());
        Glide.with(this).load(currentUser.getAvatarUrl()).into(avatar);

        nickNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateInfo();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        introductionEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateInfo();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMatissePermissions();
            }
        });
    }

    private void chooseAvatar() {
        Matisse.from(this)
                .choose(MimeType.ofAll(), false) // 选择 mime 的类型
                .countable(true)
                .maxSelectable(1) // 图片选择的最多数量
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.avatar_item_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)//图像选择和预览活动所需的方向。
                .thumbnailScale(0.85f) // 缩略图的比例
                .imageEngine(new PicassoEngine()) // 使用的图片加载引擎
                .theme(R.style.Matisse_Dracula)
                .forResult(REQUEST_CODE_IMAGE); // 设置作为标记的请求码
    }

    //编辑后更新
    private void updateInfo() {
        changeFlag = true;
        nickName.setText(nickNameEdit.getText().toString());
        introduction.setText(introductionEdit.getText().toString());
    }

    //保存修改信息到后端云
    private void saveInfo() {
        if (nickNameEdit.getText().toString().equals("")) {
            Toast.makeText(this, "用户昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUser.setNickname(nickNameEdit.getText().toString());
        currentUser.setIntroduction(introductionEdit.getText().toString());

        if (avatarChangeFlag) {
            //删除旧头像文件
            if (!currentUser.getAvatarUrl().equals(PixelApp.defaultAvatarUrl)) {
                BmobFile oldAvatarFile = new BmobFile();
                oldAvatarFile.setUrl(currentUser.getAvatarUrl());//此url是上传文件成功之后通过bmobFile.getUrl()方法获取的。
                oldAvatarFile.delete(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                        } else {
                            Toast.makeText(ProfileEditActivity.this, "旧头像文件删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            final BmobFile newAvatarFile = new BmobFile(new File(avatarPath));
            newAvatarFile.upload(new UploadFileListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        currentUser.setAvatarUrl(newAvatarFile.getFileUrl());
                        currentUser.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    Toast.makeText(ProfileEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ProfileEditActivity.this, "用户信息保存失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "用户头像上传失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            currentUser.update(new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        Toast.makeText(ProfileEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "用户信息保存失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        //更新本地缓存
        BmobUser.fetchUserInfo(new FetchUserInfoListener<BmobUser>() {
            @Override
            public void done(BmobUser user, BmobException e) {
            }
        });
    }

    //退出编辑
    //如果有修改则询问，未修改则不询问
    private void exitEdit() {
        if (changeFlag || avatarChangeFlag) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("确定要放弃修改吗？");
            dialog.setCancelable(true);
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.setNegativeButton("取消", null);
            dialog.show();
        } else {
            finish();
        }
    }

    private void requestMatissePermissions() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {
            Matisse.obtainPathResult(data);
            avatarUri = Matisse.obtainResult(data).get(0);
            avatarPath = Matisse.obtainPathResult(data).get(0);
            Glide.with(this).load(avatarUri).into(avatar);
            changeFlag = true;
            avatarChangeFlag = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意，执行操作
                chooseAvatar();
            } else {
                //用户不同意，向用户展示该权限作用
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setMessage("应用需要读取和写入外部存储来选择图片，否则部分功能可能无法使用")
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

    //Toolbar菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exitEdit();
                break;
            case R.id.edit_save:
                saveInfo();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        exitEdit();
    }
}
