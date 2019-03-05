package pers.lonestar.pixelcanvas.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

// 自定义登录界面Layout，监听布局高度的变化，
// 如果高宽比小于4:3说明此时键盘弹出，应改变布局的比例结果以保证所有元素
// 都不会被键盘遮挡
public class LoginLayout extends LinearLayout {
    boolean keyboardShowed = false;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            int width = r - l;
            int height = b - t;
            if (((float) height) / width < 4f / 3f) {
            }
        }
    }

    public LoginLayout(Context context) {
        super(context);
    }

    public LoginLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoginLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LoginLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
