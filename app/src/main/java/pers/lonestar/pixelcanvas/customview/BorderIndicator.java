package pers.lonestar.pixelcanvas.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class BorderIndicator extends View {
    private int pixelCount;
    private Paint paint;

    public BorderIndicator(Context context) {
        super(context);
        initPaint();
    }

    public BorderIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public BorderIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    public BorderIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(0, 0, getWidth(), 0, paint);
        canvas.drawLine(0, 0, 0, getWidth(), paint);
        canvas.drawLine(getWidth(), getWidth(), getWidth(), 0, paint);
        canvas.drawLine(getWidth(), getWidth(), 0, getWidth(), paint);
    }

    public void setPixelCount(int pixelCount) {
        this.pixelCount = pixelCount;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(ParameterUtils.canvasWidth / pixelCount, ParameterUtils.canvasWidth / pixelCount);
    }

    private void initPaint() {
        paint = new Paint();
        paint.setStrokeWidth(6);
        paint.setColor(getResources().getColor(R.color.borderStroke, null));
    }
}
