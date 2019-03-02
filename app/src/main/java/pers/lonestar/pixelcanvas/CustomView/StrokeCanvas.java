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
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStrokeWidth(6);
        paint.setColor(Color.BLACK);
        int width = getWidth();
        canvas.drawLine(0, 0, 0, width, paint);
        canvas.drawLine(0, 0, width, 0, paint);
        canvas.drawLine(width, width, 0, width, paint);
        canvas.drawLine(width, width, width, 0, paint);
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
}
