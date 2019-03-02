package pers.lonestar.pixelcanvas.CustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class LineCanvas extends View {
    private int pixelCount;

    public void reDrawLine() {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int pixelWidth = ParameterUtils.canvasWidth / pixelCount;
        Paint paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setColor(Color.BLACK);
        for (int i = 1; i < pixelCount; i++) {
            canvas.drawLine(0, i * pixelWidth, ParameterUtils.canvasWidth, i * pixelWidth, paint);
        }
        for (int i = 1; i < pixelCount; i++) {
            canvas.drawLine(i * pixelWidth, 0, i * pixelWidth, ParameterUtils.canvasWidth, paint);
        }
    }

    public LineCanvas(Context context) {
        super(context);
    }

    public LineCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LineCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LineCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(ParameterUtils.canvasWidth, ParameterUtils.canvasWidth);
    }

    public void setPixelCount(int pixelCount) {
        this.pixelCount = pixelCount;
    }
}
