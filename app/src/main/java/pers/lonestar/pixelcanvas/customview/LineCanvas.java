package pers.lonestar.pixelcanvas.customview;

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
    private Paint linePaint;
    private int pixelWidth;

    public void reDrawLine() {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (linePaint == null)
            initPaint();
        for (int i = 1; i < pixelCount; i++) {
            canvas.drawLine(0, i * pixelWidth, ParameterUtils.canvasWidth, i * pixelWidth, linePaint);
        }
        for (int i = 1; i < pixelCount; i++) {
            canvas.drawLine(i * pixelWidth, 0, i * pixelWidth, ParameterUtils.canvasWidth, linePaint);
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

    private void initPaint() {
        linePaint = new Paint();
        linePaint.setStrokeWidth(1);
        linePaint.setColor(Color.BLACK);
        pixelWidth = ParameterUtils.canvasWidth / pixelCount;
    }
}
