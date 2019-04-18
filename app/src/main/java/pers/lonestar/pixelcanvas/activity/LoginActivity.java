package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.SaveListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.infostore.PixelUser;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameText;
    private EditText passwordText;
    private Button loginButton;
    private Button registerButton;

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
                startActivity(intent);
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
        }
        //密码为空
        if (password.equals("")) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
