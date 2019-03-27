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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import net.margaritov.preference.colorpicker.ColorPickerDialog;

import java.text.DateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.customview.BorderIndicator;
import pers.lonestar.pixelcanvas.customview.LineCanvas;
import pers.lonestar.pixelcanvas.customview.PixelCanvas;
import pers.lonestar.pixelcanvas.customview.StrokeCanvas;
import pers.lonestar.pixelcanvas.dialog.ExportDialogFragment;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class PaintActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private LineCanvas lineCanvas;
    private PixelCanvas pixelCanvas;
    private StrokeCanvas strokeCanvas;
    //    private BackgroundCanvas backgroundCanvas;
    private FrameLayout pixelFramelayout;
    private ImageView pencil;
    private ImageView thumbnail;
    private TextView canvasName;
    private TextView canvasSize;
    private TextView canvasCreate;
    private TextView canvasUpdate;
    private BorderIndicator borderIndicator;
    private Button dotButton;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private int pixelCount;
    private int pixelSize;
    private int pencilColor;
    private int prePencilColor;
    private boolean eraserStatus = false;
    private LitePalCanvas litePalCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        //优先初始化View
        initView();
        //设置自定义Toolbar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        initListener();
        initCanvas();
        initPencil();
        createCanvasFile();
    }

    //Toolbar菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.paint_toolbar_menu, menu);
        return true;
    }

    //Toolbar菜单项监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.clearCanvas:
                clearCanvas();
                break;
            case R.id.eraser:
                setEraser();
                break;
            case R.id.moveCanvas:
                moveCanvas();
                break;
            case R.id.colorPicker:
                pickColor();
                break;
            case R.id.linetoggle:
                toggleLine();
                break;
        }
        return true;
    }

    //初始化监听
    private void initListener() {
        //导航栏菜单项监听
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.paint_nav_clear:
                        clearCanvas();
                        break;
                    case R.id.paint_nav_export:
                        //显示导出对话框
                        showExportDialog();
                        break;
                    case R.id.paint_nav_publish:
                        showPostDialog();
                        break;
                    case R.id.paint_nav_rename:
                        showRenameDialog();
                        break;
                    case R.id.paint_nav_settings:
                        Intent intent = new Intent(PaintActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.paint_nav_share:
                        shareImage();
                        break;
                }
                return true;
            }
        });
        //抽屉滑动监听，用于缩略图重绘的处理
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                //thumbnail只会在抽屉滑动时初始化一次
                if (thumbnail == null)
                    thumbnail = findViewById(R.id.thumbnail);
                if (canvasName == null)
                    canvasName = findViewById(R.id.paint_canvas_name);
                if (canvasSize == null)
                    canvasSize = findViewById(R.id.paint_canvas_size);
                if (canvasCreate == null)
                    canvasCreate = findViewById(R.id.paint_canvas_create);
                if (canvasUpdate == null)
                    canvasUpdate = findViewById(R.id.paint_canvas_update);
                thumbnail.setImageBitmap(loadBitmapFromView(pixelCanvas));
                canvasName.setText(litePalCanvas.getCanvasName());
                canvasSize.setText("尺寸:" + litePalCanvas.getPixelCount() + "x" + litePalCanvas.getPixelCount());
                canvasCreate.setText("创建时间:" + litePalCanvas.getCreatedAt());
                canvasUpdate.setText("更新时间:" + litePalCanvas.getUpdatedAt());
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    //初始化画笔，包括画笔颜色，滑动监听等
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

                        //重绘红色边框指示器
                        //确定位置
                        int border_left = left / pixelSize * pixelSize;
                        if (border_left == 960)
                            border_left = 900;
                        int border_top = bottom / pixelSize * pixelSize;
                        if (border_top == 960)
                            border_top = 900;
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
                            PixelApp.pixelColor[y][x] = pencilColor;
                            pixelCanvas.reDrawCanvas();
                        }

                        // 记录当前的位置
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        //覆盖修改
                        litePalCanvas.setJsonData(new Gson().toJson(PixelApp.pixelColor));
                        litePalCanvas.setThumbnail(ParameterUtils.bitmapToBytes(loadBitmapFromView(pixelCanvas)));
                        litePalCanvas.save();
                        break;
                }
                return true;
            }
        };
        pixelCanvas.setOnTouchListener(onTouchListener);

        //浮动按钮点击，弹出颜色选择对话框
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //更换颜色对话框库
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog(PaintActivity.this, pencilColor);
                colorPickerDialog.setAlphaSliderVisible(true);
                colorPickerDialog.setHexValueEnabled(true);
                colorPickerDialog.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int color) {
                        pencilColor = color;
                        pencil.setImageResource(R.drawable.ic_pencil);
                        pencil.setColorFilter(pencilColor, PorterDuff.Mode.MULTIPLY);
                    }
                });
                colorPickerDialog.show();
            }
        });
    }

    //初始化画布，包括边框，线条，格子数目，格子大小
    private void initCanvas() {
        Intent intent = getIntent();
        litePalCanvas = PixelApp.litePalCanvas;
        if (litePalCanvas == null) {
            //初始化画布大小
            pixelCount = intent.getIntExtra("pixelCount", 16);
            //初始化画布像素颜色信息
            PixelApp.pixelColor = new int[pixelCount][pixelCount];
        } else {
            pixelCount = litePalCanvas.getPixelCount();
            PixelApp.pixelColor = new Gson().fromJson(litePalCanvas.getJsonData(), int[][].class);
        }
        pixelSize = ParameterUtils.canvasWidth / pixelCount;

        //绘制画布背景
//        backgroundCanvas.setPixelCount(pixelCount);

        //绘制画布
        pixelCanvas.setPixelSize(pixelSize);

        //绘制画布线条
        lineCanvas.setPixelCount(pixelCount);

        //设定红色边框指示
        borderIndicator.setPixelCount(pixelCount);
    }

    //初始化View组件
    private void initView() {
        lineCanvas = findViewById(R.id.line_canvas);
        pixelCanvas = findViewById(R.id.pixel_canvas);
        strokeCanvas = findViewById(R.id.stroke_canvas);
//        backgroundCanvas = findViewById(R.id.background_canvas);
        pixelFramelayout = findViewById(R.id.paint_framelayout);
        pencil = findViewById(R.id.draw_pencil);
        borderIndicator = findViewById(R.id.border);
        dotButton = findViewById(R.id.dot_button);
        fab = findViewById(R.id.paint_activity_fab);
        toolbar = findViewById(R.id.paint_toolbar);
        navigationView = findViewById(R.id.paint_nav);
        drawerLayout = findViewById(R.id.paint_drawer);
    }


    //Toolbar菜单项方法
    //清除画布
    private void clearCanvas() {
        PixelApp.pixelColor = new int[pixelCount][pixelCount];
        pixelCanvas.reDrawCanvas();
    }

    //橡皮擦
    private void setEraser() {
        if (!eraserStatus) {
            eraserStatus = true;
            prePencilColor = pencilColor;
            pencilColor = Color.TRANSPARENT;
            pencil.setImageResource(R.drawable.ic_eraser);
            pencil.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        } else {
            eraserStatus = false;
            pencilColor = prePencilColor;
            pencil.setImageResource(R.drawable.ic_pencil);
            pencil.setColorFilter(pencilColor, PorterDuff.Mode.MULTIPLY);
        }
    }

    //取色
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
        int tmpColor = PixelApp.pixelColor[y][x];
        //绘制画笔颜色
        //不为透明色
        if (tmpColor != 0) {
            prePencilColor = pencilColor;
            pencilColor = tmpColor;
            pencil.setColorFilter(pencilColor, PorterDuff.Mode.MULTIPLY);
        }
    }

    //TODO
    //平移画布
    private void moveCanvas() {

    }

    //切换显示线条
    private void toggleLine() {
        if (lineCanvas.getVisibility() == View.INVISIBLE) {
//            backgroundCanvas.setVisibility(View.INVISIBLE);
            lineCanvas.setVisibility(View.VISIBLE);
        } else {
//            backgroundCanvas.setVisibility(View.INVISIBLE);
            lineCanvas.setVisibility(View.INVISIBLE);
        }
    }


    //导航栏菜单项方法

    //获取View的Bitmap，用于图像生成和设置
    private Bitmap loadBitmapFromView(View view) {
        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.TRANSPARENT);
        view.draw(c);
        return bmp;
    }

    //TODO
    //生成画布文件
    private void createCanvasFile() {
        //获取本地日期时间格式
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        if (litePalCanvas == null) {
            litePalCanvas = new LitePalCanvas();
            PixelApp.litePalCanvas = litePalCanvas;
            litePalCanvas.setCanvasName("无题");
            litePalCanvas.setPixelCount(pixelCount);
            litePalCanvas.setCreator(PixelApp.pixelUser.getNickname());
            litePalCanvas.setCreatorID(PixelApp.pixelUser.getObjectId());
            litePalCanvas.setCreatedAt(dateFormat.format(date));
            litePalCanvas.setUpdatedAt(dateFormat.format(date));
            litePalCanvas.setJsonData(new Gson().toJson(PixelApp.pixelColor));
            litePalCanvas.save();
        } else {
            //TODO
            litePalCanvas.setUpdatedAt(dateFormat.format(date));
        }
    }

    //发布作品
    private void postCanvasFile() {
        BmobCanvas bmobCanvas = new BmobCanvas();
        bmobCanvas.setCanvasName(litePalCanvas.getCanvasName());
        bmobCanvas.setCreator(litePalCanvas.getCreator());
        bmobCanvas.setCreatorID(litePalCanvas.getCreatorID());
        bmobCanvas.setPixelCount(litePalCanvas.getPixelCount());
        bmobCanvas.setJsonData(litePalCanvas.getJsonData());
        bmobCanvas.setThumbnail(litePalCanvas.getThumbnail());

        bmobCanvas.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    Toast.makeText(PaintActivity.this, "作品发布成功！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PaintActivity.this, "作品发布失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //TODO
    //分享功能，考虑生成多种格式分享
    private void shareImage() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
    }

    //导出对话框
    private void showExportDialog() {
        ExportDialogFragment fragment = new ExportDialogFragment();
        fragment.initParameter(litePalCanvas, pixelCanvas);
        fragment.show(getSupportFragmentManager(), "ExportDialog");
    }

    //发布对话框
    private void showPostDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(PaintActivity.this);
        dialog.setMessage("确定要发布这个作品吗？");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postCanvasFile();
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    //重命名对话框
    private void showRenameDialog() {
        View view = View.inflate(PaintActivity.this, R.layout.rename_dialog, null);
        final EditText renameText = view.findViewById(R.id.rename_text);
        renameText.setText(litePalCanvas.getCanvasName());
        renameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameText.selectAll();
            }
        });
        AlertDialog.Builder dialog = new AlertDialog.Builder(PaintActivity.this);
        dialog.setView(view);
        dialog.setTitle("重命名");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = renameText.getText().toString();
                if (!newName.equals("")) {
                    litePalCanvas.setCanvasName(newName);
                    litePalCanvas.save();
                } else {
                    Toast.makeText(PaintActivity.this, "名称不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //覆盖修改
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        Date date = new Date(System.currentTimeMillis());
        litePalCanvas.setThumbnail(ParameterUtils.bitmapToBytes(loadBitmapFromView(pixelCanvas)));
        litePalCanvas.setUpdatedAt(dateFormat.format(date));
        litePalCanvas.save();
        PixelApp.pixelColor = null;
        PixelApp.litePalCanvas = null;
        finish();
    }
}