package pers.lonestar.pixelcanvas.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class BackgroundCanvas extends View {
    private Paint whitePaint;
    private Paint grayPaint;
    private int pixelWidth;
    private int pixelCount;

    public BackgroundCanvas(Context context) {
        super(context);
    }

    public BackgroundCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BackgroundCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BackgroundCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //初始化背景画笔
        if (whitePaint == null || grayPaint == null)
            iniPaint();
        Paint tmpPaint;
        for (int i = 0; i < pixelCount; i++) {
            for (int j = 0; j < pixelCount; j++) {
                if ((i & 1) == 1) {
                    if ((j & 1) == 1)
                        tmpPaint = grayPaint;
                    else
                        tmpPaint = whitePaint;
                } else {
                    if ((j & 1) == 1)
                        tmpPaint = whitePaint;
                    else
                        tmpPaint = grayPaint;
                }
                canvas.drawRect(j * pixelWidth, i * pixelWidth, (j + 1) * pixelWidth, (i + 1) * pixelWidth, tmpPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(ParameterUtils.canvasWidth, ParameterUtils.canvasWidth);
    }

    public void setPixelCount(int pixelCount) {
        this.pixelCount = pixelCount;
    }

    public void reDrawBackground() {
        invalidate();
    }

    private void iniPaint() {
        pixelWidth = ParameterUtils.canvasWidth / pixelCount;
        whitePaint = new Paint();
        whitePaint.setColor(Color.rgb(192, 192, 192));
        whitePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        grayPaint = new Paint();
        grayPaint.setColor(Color.rgb(128, 128, 128));
        grayPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }
}
