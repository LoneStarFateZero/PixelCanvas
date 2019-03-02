package pers.lonestar.pixelcanvas.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import pers.lonestar.pixelcanvas.CustomView.BorderIndicator;
import pers.lonestar.pixelcanvas.CustomView.LineCanvas;
import pers.lonestar.pixelcanvas.CustomView.PixelCanvas;
import pers.lonestar.pixelcanvas.CustomView.StrokeCanvas;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class PaintActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private LineCanvas lineCanvas;
    private PixelCanvas pixelCanvas;
    private StrokeCanvas strokeCanvas;
    private ImageView pencil;
    private BorderIndicator borderIndicator;
    private Button dotButton;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private int pixelCount;
    private int pixelSize;
    private int pencilColor;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.paint_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clearCanvas:
                clearCanvas();
                break;
            case R.id.moveCanvas:
                moveCanvas();
                break;
            case R.id.colorPicker:
                pickColor();
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        initView();

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);
        initListener();
        initCanvas();
        initPencil();
    }

    private void initListener() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.paint_nav_clear:
                        clearCanvas();
                        break;
                    case R.id.paint_nav_export:
                        break;
                    case R.id.paint_nav_publish:
                        break;
                    case R.id.paint_nav_rename:
                        break;
                    case R.id.paint_nav_settings:
                        break;
                    case R.id.paint_nav_share:
                        break;
                }
                return true;
            }
        });
    }

    private void initPencil() {
        pencilColor = Color.BLACK;
        pencil.setColorFilter(pencilColor, PorterDuff.Mode.MULTIPLY);
        //画布上的铅笔滑动监听
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            int lastX, lastY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;//位移量X
                        int dy = (int) event.getRawY() - lastY;//位移量Y

                        //画笔新位置=原位置+偏移量
                        int left = pencil.getLeft() + dx;
                        int top = pencil.getTop() + dy;
                        int right = pencil.getRight() + dx;
                        int bottom = pencil.getBottom() + dy;

                        //防止超出边界
                        if (left < 0) {
                            left = 0;
                            right = left + pencil.getWidth();

                        }
                        if (right > (v.getHeight() + pencil.getWidth())) {
                            right = v.getHeight() + pencil.getWidth();
                            left = right - pencil.getWidth();

                        }
                        if (top < (0 - pencil.getHeight())) {
                            top = 0 - pencil.getHeight();
                            bottom = top + pencil.getHeight();

                        }
                        if (bottom > v.getHeight()) {
                            bottom = v.getHeight();
                            top = bottom - pencil.getHeight();

                        }

                        pencil.layout(left, top, right, bottom);
                        pencil.postInvalidate();

                        //TODO
                        //重绘红色边框指示器
                        //确定位置
                        int border_left = left / pixelSize * pixelSize;
                        int border_top = bottom / pixelSize * pixelSize;
                        int border_bottom = border_top + borderIndicator.getHeight();
                        int border_right = border_left + borderIndicator.getWidth();
                        borderIndicator.layout(border_left, border_top, border_right, border_bottom);
                        borderIndicator.postInvalidate();

                        //边框位置也可用于画图
                        if (dotButton.isPressed()) {
                            int x = left / pixelSize;
                            int y = bottom / pixelSize;
                            //防止数组越界
                            if (x >= pixelCount) {
                                x = pixelCount - 1;
                            }
                            if (y >= pixelCount) {
                                y = pixelCount - 1;
                            }
                            ParameterUtils.pixelColor[y][x] = pencilColor;
                            pixelCanvas.setPixelSize(pixelSize);
                            pixelCanvas.invalidate();

                            ImageView thumbnail = findViewById(R.id.thumbnail);
                            if (thumbnail != null)
                                thumbnail.setImageBitmap(loadBitmapFromView(pixelCanvas));
                        }

                        // 记录当前的位置
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                }
                return true;
            }
        };

        lineCanvas.setOnTouchListener(onTouchListener);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogBuilder.with(PaintActivity.this).setTitle("Choose Color")
                        .initialColor(pencilColor).wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12).setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                    }
                }).showAlphaSlider(false).showColorPreview(true).setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int selectedColor, Integer[] integers) {
                        pencilColor = selectedColor;
                        pencil.setColorFilter(pencilColor, PorterDuff.Mode.MULTIPLY);
                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).build().show();

            }
        });
    }

    private void initCanvas() {
        Intent intent = getIntent();
        pixelCount = intent.getIntExtra("pixelCount", 16);
        pixelSize = ParameterUtils.canvasWidth / pixelCount;

        //绘制画布线条
        lineCanvas.setPixelCount(pixelCount);
        lineCanvas.reDrawLine();

        //设定红色边框指示
        borderIndicator.setPixelCount(pixelCount);
        borderIndicator.reDrawLine();

        ParameterUtils.pixelColor = new int[pixelCount][pixelCount];
    }

    private void initView() {
        lineCanvas = findViewById(R.id.line_canvas);
        pixelCanvas = findViewById(R.id.pixel_canvas);
        strokeCanvas = findViewById(R.id.stroke_canvas);
        pencil = findViewById(R.id.draw_pencil);
        borderIndicator = findViewById(R.id.border);
        dotButton = findViewById(R.id.dot_button);
        fab = findViewById(R.id.paint_activity_fab);
        toolbar = findViewById(R.id.paint_toolbar);
        navigationView = findViewById(R.id.paint_nav);
        drawerLayout = findViewById(R.id.paint_drawer);
    }

    private void clearCanvas() {
        ParameterUtils.pixelColor = new int[pixelCount][pixelCount];
        pixelCanvas.invalidate();
    }

    //TODO
    private void pickColor() {
        int left = pencil.getLeft();
        int bottom = pencil.getBottom();
        int x = left / pixelSize;
        int y = bottom / pixelSize;
        if (x >= pixelCount)
            x = pixelCount - 1;
        if (y >= pixelCount)
            y = pixelCount - 1;
        //获取该点颜色
        pencilColor = ParameterUtils.pixelColor[y][x];
        //绘制画笔颜色
        //不为透明色
        if (pencilColor != 0) {
            pencil.setColorFilter(pencilColor, PorterDuff.Mode.MULTIPLY);
        } else {
            //若取色为透明，则画笔色设为白色
            pencilColor = Color.WHITE;
            pencil.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        }
    }

    //TODO
    private void moveCanvas() {

    }

    private Bitmap loadBitmapFromView(View view) {
        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        view.layout(0, 0, view.getWidth(), view.getHeight());
        view.draw(c);
        return bmp;
    }
}
