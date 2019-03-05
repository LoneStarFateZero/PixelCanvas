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
import cn.bmob.v3.listener.SaveListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.infostore.PixelUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameText;
    private EditText passwordText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameText = findViewById(R.id.register_username_text);
        passwordText = findViewById(R.id.register_password_text);
        registerButton = findViewById(R.id.finish_register_button);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = usernameText.getText().toString();
                String password = passwordText.getText().toString();
                PixelUser pixelUser = new PixelUser();
                pixelUser.setUsername(userName);
                pixelUser.setEmail(userName);
                pixelUser.setNickname(userName);
                pixelUser.setPassword(password);
                pixelUser.signUp(new SaveListener<PixelUser>() {
                    @Override
                    public void done(PixelUser pixelUser, BmobException e) {
                        if (e == null) {
                            Toast.makeText(RegisterActivity.this, "注册成功：" + BmobUser.getCurrentUser(PixelUser.class).getNickname(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
