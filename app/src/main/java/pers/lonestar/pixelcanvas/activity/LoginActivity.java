package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.infostore.PixelUser;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameText;
    private EditText passwordText;
    private Button loginButton;
    private Button registerButton;
    private Button retrieveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //本地是否有用户缓存
        if (BmobUser.isLogin()) {
            fetchUserInfo();
            Intent intent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(LoginActivity.this, "尚未登录，请先登录", Toast.LENGTH_SHORT).show();
        }
        usernameText = findViewById(R.id.username_text);
        passwordText = findViewById(R.id.password_text);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        retrieveButton = findViewById(R.id.retrieve_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = usernameText.getText().toString();
                String password = passwordText.getText().toString();
                if (!checkInfo(userName, password))
                    return;
                PixelUser pixelUser = new PixelUser();
                pixelUser.setUsername(userName);
                pixelUser.setPassword(password);
                pixelUser.login(new SaveListener<PixelUser>() {
                    @Override
                    public void done(PixelUser pixelUser, BmobException e) {
                        if (e == null) {
                            Toast.makeText(LoginActivity.this, "登录成功：" + BmobUser.getCurrentUser(PixelUser.class).getNickname(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            if (e.getErrorCode() == 101)
                                Toast.makeText(LoginActivity.this, "用户名或密码不正确", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(LoginActivity.this, "登录失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra("user_name", usernameText.getText().toString());
                intent.putExtra("user_password", passwordText.getText().toString());
                startActivity(intent);
            }
        });

        retrieveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userName = usernameText.getText().toString();
                if (userName.equals("")) {
                    Toast.makeText(LoginActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    BmobQuery<PixelUser> bmobQuery = new BmobQuery<>();
                    bmobQuery.addWhereEqualTo("username", userName);
                    bmobQuery.findObjects(new FindListener<PixelUser>() {
                        @Override
                        public void done(List<PixelUser> object, BmobException e) {
                            if (e == null) {
                                //用户不存在
                                if (object.size() == 0) {
                                    Toast.makeText(LoginActivity.this, "用户不存在", Toast.LENGTH_SHORT).show();
                                }
                                //用户存在
                                else {
                                    BmobUser.resetPasswordByEmail(userName, new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                Toast.makeText(LoginActivity.this, "重置密码邮件发送成功，请到" + userName + "邮箱进行密码重置操作", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "重置密码邮件发送失败，请检查网络设置后重试", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "重置密码邮件发送失败，请检查网络设置后重试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void fetchUserInfo() {
        BmobUser.fetchUserInfo(new FetchUserInfoListener<BmobUser>() {
            @Override
            public void done(BmobUser user, BmobException e) {
                if (e == null) {
                    Toast.makeText(LoginActivity.this, "登录成功：" + BmobUser.getCurrentUser(PixelUser.class).getNickname(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "更新本地缓存用户信息失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkInfo(String userName, String password) {
        //用户名为空
        if (userName.equals("")) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(userName);
            if (!matcher.matches()) {
                Toast.makeText(this, "用户名邮箱地址不合法", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        //密码为空
        if (password.equals("")) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
