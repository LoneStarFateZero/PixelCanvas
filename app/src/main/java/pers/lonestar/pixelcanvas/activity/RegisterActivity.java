package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.infostore.PixelUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameText;
    private EditText passwordText;
    private EditText secondPasswordText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Intent intent = getIntent();
        String userName = intent.getStringExtra("user_name");
        String userPassword = intent.getStringExtra("user_password");

        usernameText = findViewById(R.id.register_username_text);
        usernameText.setText(userName);
        passwordText = findViewById(R.id.register_password_text);
        passwordText.setText(userPassword);
        secondPasswordText = findViewById(R.id.register_second_password_text);
        registerButton = findViewById(R.id.finish_register_button);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = usernameText.getText().toString();
                String password = passwordText.getText().toString();
                String secondPassword = secondPasswordText.getText().toString();

                if (!checkInfo(userName, password, secondPassword)) {
                    return;
                }

                PixelUser pixelUser = new PixelUser();
                pixelUser.setAvatarUrl(PixelApp.defaultAvatarUrl);
                pixelUser.setUsername(userName);
                pixelUser.setEmail(userName);
                pixelUser.setNickname("新用户" + userName);
                pixelUser.setIntroduction("编辑个人简介");
                pixelUser.setPassword(password);
                pixelUser.signUp(new SaveListener<PixelUser>() {
                    @Override
                    public void done(PixelUser pixelUser, BmobException e) {
                        if (e == null) {
                            Toast.makeText(RegisterActivity.this, "注册成功：" + BmobUser.getCurrentUser(PixelUser.class).getNickname(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            if (e.getErrorCode() == 202)
                                Toast.makeText(RegisterActivity.this, "此用户名已被使用", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(RegisterActivity.this, "注册失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private boolean checkInfo(String userName, String password, String secnodPassword) {
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
        //二次密码不正确
        if (!secnodPassword.equals(password)) {
            Toast.makeText(this, "密码不匹配", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
