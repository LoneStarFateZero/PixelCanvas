package pers.lonestar.pixelcanvas.CustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class StrokeCanvas extends View {
    private Paint strokePaint;
    private int strokeWidth;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (strokePaint == null)
            iniPaint();
        canvas.drawLine(0, 0, 0, strokeWidth, strokePaint);
        canvas.drawLine(0, 0, strokeWidth, 0, strokePaint);
        canvas.drawLine(strokeWidth, strokeWidth, 0, strokeWidth, strokePaint);
        canvas.drawLine(strokeWidth, strokeWidth, strokeWidth, 0, strokePaint);
    }

    public StrokeCanvas(Context context) {
        super(context);
    }

    public StrokeCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StrokeCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StrokeCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(ParameterUtils.canvasWidth, ParameterUtils.canvasWidth);
    }

    private void iniPaint() {
        strokePaint = new Paint();
        strokePaint.setStrokeWidth(6);
        strokePaint.setColor(Color.BLACK);
        strokeWidth = getWidth();
    }
}
