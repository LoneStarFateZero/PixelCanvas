package pers.lonestar.pixelcanvas.CustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class PixelCanvas extends View {
    private int pixelSize;

    @Override
    protected void onDraw(Canvas canvas) {
        drawPixel(canvas);
    }

    //设定画布长宽
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(ParameterUtils.canvasWidth, ParameterUtils.canvasWidth);
    }

    //自定义绘制
    private void drawPixel(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(1);
        for (int i = 0; i < ParameterUtils.pixelColor.length; i++) {
            for (int j = 0; j < ParameterUtils.pixelColor.length; j++) {
                //此处重绘若不加判断，绘制矩形会极为耗时
                //若不为白色，才绘制矩形，否则默认白色
                if (ParameterUtils.pixelColor[i][j] != 0) {
                    paint.setColor(ParameterUtils.pixelColor[i][j]);
                    int left = j * pixelSize;
                    int top = i * pixelSize;
                    canvas.drawRect(left, top, left + pixelSize, top + pixelSize, paint);
                }
            }
        }
    }

    public PixelCanvas(Context context) {
        super(context);
    }

    public PixelCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PixelCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PixelCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setPixelSize(int pixelSize) {
        this.pixelSize = pixelSize;
    }
}
