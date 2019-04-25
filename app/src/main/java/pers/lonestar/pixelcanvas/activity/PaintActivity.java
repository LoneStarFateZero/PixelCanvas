package pers.lonestar.pixelcanvas.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import net.margaritov.preference.colorpicker.ColorPickerDialog;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;
import java.util.Stack;

import cn.bmob.v3.BmobUser;
import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.customview.BorderIndicator;
import pers.lonestar.pixelcanvas.customview.LineCanvas;
import pers.lonestar.pixelcanvas.customview.PixelCanvas;
import pers.lonestar.pixelcanvas.customview.StrokeCanvas;
import pers.lonestar.pixelcanvas.dialog.ExportDialogFragment;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class PaintActivity extends AppCompatActivity {
    private static final int SHAPE_DOT = 0;
    private static final int SHAPE_LINE = 1;
    private static final int SHAPE_RECT = 2;
    private static final int SHAPE_RECT_FILLED = 3;
    private int REQUEST_CODE_PERMISSION = 1997;
    private DrawerLayout drawerLayout;
    private RelativeLayout screenLayout;
    private LineCanvas lineCanvas;
    private PixelCanvas pixelCanvas;
    private StrokeCanvas strokeCanvas;
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
    private View moveUpView;
    private View moveDownView;
    private View moveLeftView;
    private View moveRightView;
    private Menu mMenu;
    private int pencilShape = SHAPE_DOT;
    private int pencilPreX = -1;
    private int pencilPreY = -1;
    private int pixelCount;
    private int pixelSize;
    private int pencilColor;
    private int prePencilColor;
    private boolean eraserStatus = false;
    private LitePalCanvas litePalCanvas;
    private boolean canvasChangeFlag = false;
    private boolean globalChangeFlag = false;
    private Stack<int[][]> pixelColorUndoStack;
    private Stack<int[][]> pixelColorRedoStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        //优先初始化View
        initView();

        initListener();
        initCanvas();
        initPencil();
        createCanvasFile();
    }

    //Toolbar菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.paint_toolbar_menu, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (pencilShape) {
            case SHAPE_DOT:
                mMenu.findItem(R.id.pencil_shape).setIcon(R.drawable.ic_shape_dot);
                break;
//            case SHAPE_LINE:
//                mMenu.findItem(R.id.pencil_shape).setIcon(R.drawable.ic_shape_line);
//                break;
            case SHAPE_RECT:
                mMenu.findItem(R.id.pencil_shape).setIcon(R.drawable.ic_shape_rect);
                break;
            case SHAPE_RECT_FILLED:
                mMenu.findItem(R.id.pencil_shape).setIcon(R.drawable.ic_shape_rect_fill);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //Toolbar菜单项监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.undo:
                undoCanvas();
                break;
            case R.id.redo:
                redoCanvas();
                break;
            case R.id.eraser:
                setEraser();
                break;
            case R.id.paint_bucket:
                paintBucket();
                break;
            case R.id.pencil_shape:
                changeShape();
                break;
            case R.id.colorPicker:
                pickColor();
                break;
            case R.id.linetoggle:
                toggleLine();
                break;
            case R.id.paint_move:
                moveCanvas();
                break;
        }
        pencilPreX = -1;
        pencilPreY = -1;
        return true;
    }

    //初始化监听
    private void initListener() {
        screenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toolbar.getVisibility() == View.VISIBLE) {
                    toolbar.setVisibility(View.INVISIBLE);
                } else {
                    toolbar.setVisibility(View.VISIBLE);
                }
            }
        });

        //导航栏菜单项监听
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.paint_nav_clear:
                        clearCanvas();
                        break;
                    case R.id.paint_nav_export:
                        //显示导出对话框
                        requestExportPermissions();
                        break;
                    //作品发布
                    //如果进入后不做其他操作直接发布，会出现缩略图为空的异常
                    ///TODO
                    case R.id.paint_nav_publish:
                        //创建后未作出任何修改直接返回
                        //不及时保存画布数据可能造成空指针异常
                        litePalCanvas.setJsonData(new Gson().toJson(PixelApp.pixelColor));
                        litePalCanvas.setThumbnail(ParameterUtils.bitmapToBytes(loadBitmapFromView(pixelCanvas)));
                        litePalCanvas.save();
                        Intent postIntent = new Intent(PaintActivity.this, PublishActivity.class);
                        postIntent.putExtra("local_canvas", litePalCanvas);
                        GalleryActivity.getInstance().startActivity(postIntent);
                        break;
                    case R.id.paint_nav_rename:
                        showRenameDialog();
                        break;
                    case R.id.paint_nav_share:
                        shareCanvas();
                        break;
                }
                pencilPreX = -1;
                pencilPreY = -1;
                drawerLayout.closeDrawers();
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
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
                thumbnail.setImageBitmap(loadBitmapFromView(pixelFramelayout));
                canvasName.setText(litePalCanvas.getCanvasName());
                canvasSize.setText("尺寸:\n" + litePalCanvas.getPixelCount() + "x" + litePalCanvas.getPixelCount());
                canvasCreate.setText("创建时间:\n" + litePalCanvas.getCreatedAt());
                canvasUpdate.setText("更新时间:\n" + litePalCanvas.getUpdatedAt());
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        //画布上移
        moveUpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //全局修改标识
                globalChangeFlag = true;
                //颜色二维数组移动
                int[] tmpPixelColorRow = new int[pixelCount];
                System.arraycopy(PixelApp.pixelColor[0], 0, tmpPixelColorRow, 0, pixelCount);
                for (int i = 0; i < pixelCount - 1; i++) {
                    System.arraycopy(PixelApp.pixelColor[i + 1], 0, PixelApp.pixelColor[i], 0, pixelCount);
                }
                System.arraycopy(tmpPixelColorRow, 0, PixelApp.pixelColor[pixelCount - 1], 0, pixelCount);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //入撤销栈
                        pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
                        //清除恢复栈
                        pixelColorRedoStack.clear();
                    }
                }).start();
                //画布重绘
                pixelCanvas.reDrawCanvas();
            }
        });
        //画布下移
        moveDownView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalChangeFlag = true;

                int[] tmpPixelColorRow = new int[pixelCount];
                System.arraycopy(PixelApp.pixelColor[pixelCount - 1], 0, tmpPixelColorRow, 0, pixelCount);
                for (int i = pixelCount - 1; i > 0; i--) {
                    System.arraycopy(PixelApp.pixelColor[i - 1], 0, PixelApp.pixelColor[i], 0, pixelCount);
                }
                System.arraycopy(tmpPixelColorRow, 0, PixelApp.pixelColor[0], 0, pixelCount);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
                        pixelColorRedoStack.clear();
                    }
                }).start();
                pixelCanvas.reDrawCanvas();
            }
        });
        //画布左移
        moveLeftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalChangeFlag = true;

                int[] tmpPixelColorRow = new int[pixelCount];
                for (int i = 0; i < pixelCount; i++) {
                    tmpPixelColorRow[i] = PixelApp.pixelColor[i][0];
                }
                for (int i = 0; i < pixelCount; i++) {
                    for (int j = 0; j < pixelCount - 1; j++) {
                        PixelApp.pixelColor[i][j] = PixelApp.pixelColor[i][j + 1];
                    }
                }
                for (int i = 0; i < pixelCount; i++) {
                    PixelApp.pixelColor[i][pixelCount - 1] = tmpPixelColorRow[i];
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
                        pixelColorRedoStack.clear();
                    }
                }).start();
                pixelCanvas.reDrawCanvas();
            }
        });
        //画布右移
        moveRightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalChangeFlag = true;

                int[] tmpPixelColorRow = new int[pixelCount];
                for (int i = 0; i < pixelCount; i++) {
                    tmpPixelColorRow[i] = PixelApp.pixelColor[i][pixelCount - 1];
                }
                for (int i = 0; i < pixelCount; i++) {
                    for (int j = pixelCount - 1; j > 0; j--) {
                        PixelApp.pixelColor[i][j] = PixelApp.pixelColor[i][j - 1];
                    }
                }
                for (int i = 0; i < pixelCount; i++) {
                    PixelApp.pixelColor[i][0] = tmpPixelColorRow[i];
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
                        pixelColorRedoStack.clear();
                    }
                }).start();
                pixelCanvas.reDrawCanvas();
            }
        });
        View.OnTouchListener fabOnTouchListener = new View.OnTouchListener() {
            int lastX, lastY;
            int left = 0, top = 0, right = 0, bottom = 0;
            boolean isMoved;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        isMoved = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        isMoved = true;
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        left = v.getLeft() + dx;
                        top = v.getTop() + dy;
                        right = v.getRight() + dx;
                        bottom = v.getBottom() + dy;

                        //防止超出边界
                        if (left < 0) {
                            left = 0;
                            right = left + v.getWidth();
                        }
                        if (right > screenLayout.getWidth()) {
                            right = screenLayout.getWidth();
                            left = right - v.getWidth();
                        }
                        if (top < 0) {
                            top = 0;
                            bottom = top + v.getHeight();
                        }
                        if (bottom > screenLayout.getHeight()) {
                            bottom = screenLayout.getHeight();
                            top = bottom - v.getHeight();
                        }

                        v.layout(left, top, right, bottom);
                        // 记录当前的位置
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!isMoved) {
                            if (!eraserStatus) {
                                //弹出颜色选择器
                                ColorPickerDialog colorPickerDialog = new ColorPickerDialog(PaintActivity.this, pencilColor);
                                colorPickerDialog.setAlphaSliderVisible(true);
                                colorPickerDialog.setHexValueEnabled(true);
                                colorPickerDialog.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                                    @Override
                                    public void onColorChanged(int color) {
                                        prePencilColor = pencilColor;
                                        pencilColor = color;
                                        pencil.setImageResource(R.drawable.ic_pencil);
                                        pencil.setColorFilter(pencilColor, PorterDuff.Mode.MULTIPLY);
                                    }
                                });
                                colorPickerDialog.show();
                            } else {
                                Toast.makeText(PaintActivity.this, "请先将橡皮擦切换为画笔", Toast.LENGTH_SHORT).show();
                            }
                        } else {
//                            RelativeLayout.LayoutParams fabParams =
////                                    (RelativeLayout.LayoutParams) v.getLayoutParams();
                            RelativeLayout.LayoutParams fabParams = new RelativeLayout.LayoutParams(v.getWidth(), v.getHeight());
                            fabParams.setMargins(left, top, screenLayout.getWidth() - right, screenLayout.getHeight() - bottom);    //控件相对父控件左上右下的距离
                            fabParams.width = v.getWidth();
                            fabParams.height = v.getHeight();
                            v.setLayoutParams(fabParams);
                        }
                        break;
                }
                return true;
            }
        };
        //浮动按钮点击，弹出颜色选择对话框
        fab.setOnTouchListener(fabOnTouchListener);

        dotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画笔新位置=原位置+偏移量
                int penleft = pencil.getLeft();
                int penbottom = pencil.getBottom();
                int penx = penleft / pixelSize;
                int peny = penbottom / pixelSize;
                //防止数组越界
                if (penx >= pixelCount) {
                    penx = pixelCount - 1;
                }
                if (peny >= pixelCount) {
                    peny = pixelCount - 1;
                }
                if (pencilPreX == -1 && pencilPreY == -1) {
                    pencilPreX = penx;
                    pencilPreY = peny;
                    PixelApp.pixelColor[peny][penx] = pencilColor;
                    pixelCanvas.reDrawCanvas();
                    //保存入栈
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
                            pixelColorRedoStack.clear();
                        }
                    }).start();
                } else {
                    switch (pencilShape) {
                        case SHAPE_DOT:
                            pencilPreX = -1;
                            pencilPreY = -1;
                            PixelApp.pixelColor[peny][penx] = pencilColor;
                            pixelCanvas.reDrawCanvas();
                            //保存入栈
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
                                    pixelColorRedoStack.clear();
                                }
                            }).start();
                            break;
                        case SHAPE_LINE:
                            break;
                        case SHAPE_RECT:
                            if (pencilPreX >= penx && pencilPreY >= peny) {
                                for (int i = peny; i <= pencilPreY; i++) {
                                    PixelApp.pixelColor[i][pencilPreX] = pencilColor;
                                    PixelApp.pixelColor[i][penx] = pencilColor;
                                }
                                for (int i = penx; i <= pencilPreX; i++) {
                                    PixelApp.pixelColor[pencilPreY][i] = pencilColor;
                                    PixelApp.pixelColor[peny][i] = pencilColor;
                                }
                            } else if (pencilPreX >= penx) {
                                for (int i = pencilPreY; i <= peny; i++) {
                                    PixelApp.pixelColor[i][pencilPreX] = pencilColor;
                                    PixelApp.pixelColor[i][penx] = pencilColor;
                                }
                                for (int i = penx; i <= pencilPreX; i++) {
                                    PixelApp.pixelColor[pencilPreY][i] = pencilColor;
                                    PixelApp.pixelColor[peny][i] = pencilColor;
                                }
                            } else if (pencilPreY >= peny) {
                                for (int i = peny; i <= pencilPreY; i++) {
                                    PixelApp.pixelColor[i][pencilPreX] = pencilColor;
                                    PixelApp.pixelColor[i][penx] = pencilColor;
                                }
                                for (int i = pencilPreX; i <= penx; i++) {
                                    PixelApp.pixelColor[pencilPreY][i] = pencilColor;
                                    PixelApp.pixelColor[peny][i] = pencilColor;
                                }
                            } else {
                                for (int i = pencilPreY; i <= peny; i++) {
                                    PixelApp.pixelColor[i][pencilPreX] = pencilColor;
                                    PixelApp.pixelColor[i][penx] = pencilColor;
                                }
                                for (int i = pencilPreX; i <= penx; i++) {
                                    PixelApp.pixelColor[pencilPreY][i] = pencilColor;
                                    PixelApp.pixelColor[peny][i] = pencilColor;
                                }
                            }
                            pencilPreX = -1;
                            pencilPreY = -1;
                            pixelCanvas.reDrawCanvas();
                            //保存入栈
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
                                    pixelColorRedoStack.clear();
                                }
                            }).start();
                            break;
                        case SHAPE_RECT_FILLED:
                            if (pencilPreX >= penx && pencilPreY >= peny) {
                                for (int i = peny; i <= pencilPreY; i++) {
                                    for (int j = penx; j <= pencilPreX; j++) {
                                        PixelApp.pixelColor[i][j] = pencilColor;
                                    }
                                }
                            } else if (pencilPreX >= penx) {
                                for (int i = pencilPreY; i <= peny; i++) {
                                    for (int j = penx; j <= pencilPreX; j++) {
                                        PixelApp.pixelColor[i][j] = pencilColor;
                                    }
                                }
                            } else if (pencilPreY >= peny) {
                                for (int i = peny; i <= pencilPreY; i++) {
                                    for (int j = pencilPreX; j <= penx; j++) {
                                        PixelApp.pixelColor[i][j] = pencilColor;
                                    }
                                }
                            } else {
                                for (int i = pencilPreY; i <= peny; i++) {
                                    for (int j = pencilPreX; j <= penx; j++) {
                                        PixelApp.pixelColor[i][j] = pencilColor;
                                    }
                                }
                            }
                            pencilPreX = -1;
                            pencilPreY = -1;
                            pixelCanvas.reDrawCanvas();
                            //保存入栈
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
                                    pixelColorRedoStack.clear();
                                }
                            }).start();
                            break;
                    }
                }
            }
        });
    }

    //初始化画笔，包括画笔颜色，滑动监听等
    private void initPencil() {
        pencilColor = Color.parseColor("#CA3628");
        pencil.setColorFilter(pencilColor, PorterDuff.Mode.MULTIPLY);
        //画布上的铅笔滑动监听
        View.OnTouchListener pencilOnTouchListener = new View.OnTouchListener() {
            int lastX, lastY;
            int left, top, right, bottom;
            int border_left, border_top, border_right, border_bottom;

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
                        left = pencil.getLeft() + dx;
                        top = pencil.getTop() + dy;
                        right = pencil.getRight() + dx;
                        bottom = pencil.getBottom() + dy;

                        //防止超出边界
                        if (left < 0) {
                            left = 0;
                            right = left + pencil.getWidth();

                        }
                        if (right > (v.getWidth() + pencil.getWidth())) {
                            right = v.getWidth() + pencil.getWidth();
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

                        //重绘红色边框指示器
                        //确定位置
                        border_left = left / pixelSize * pixelSize;
                        if (border_left == 960)
                            border_left -= pixelSize;
                        border_top = bottom / pixelSize * pixelSize;
                        if (border_top == 960)
                            border_top -= pixelSize;
                        border_bottom = border_top + borderIndicator.getHeight();
                        border_right = border_left + borderIndicator.getWidth();

                        borderIndicator.layout(border_left, border_top, border_right, border_bottom);

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
                            //同色像素块不必重新绘制
                            if (PixelApp.pixelColor[y][x] != pencilColor) {
                                canvasChangeFlag = true;
                                globalChangeFlag = true;
                                PixelApp.pixelColor[y][x] = pencilColor;
                                pixelCanvas.reDrawCanvas();
                            }
                        }
                        //按键松开算是完成一次绘制
                        else {
                            //如果发生修改动作才进行覆盖保存
                            if (canvasChangeFlag) {
                                canvasChangeFlag = false;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
                                        pixelColorRedoStack.clear();
                                        litePalCanvas.setJsonData(new Gson().toJson(PixelApp.pixelColor));
                                        litePalCanvas.setThumbnail(ParameterUtils.bitmapToBytes(loadBitmapFromView(pixelCanvas)));
                                        litePalCanvas.save();
                                    }
                                }).start();
                            }
                        }

                        // 记录当前的位置
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        FrameLayout.LayoutParams pencilParams = new FrameLayout.LayoutParams(pencil.getWidth(), pencil.getHeight());
                        pencilParams.setMargins(left, top, pixelFramelayout.getWidth() - right, pixelFramelayout.getHeight() - bottom);    //控件相对父控件左上右下的距离
                        pencil.setLayoutParams(pencilParams);

                        FrameLayout.LayoutParams borderParams = new FrameLayout.LayoutParams(borderIndicator.getWidth(), borderIndicator.getHeight());
                        borderParams.setMargins(border_left, border_top, pixelFramelayout.getWidth() - border_right, pixelFramelayout.getHeight() - border_bottom);    //控件相对父控件左上右下的距离
                        borderIndicator.setLayoutParams(borderParams);

                        if (!dotButton.isPressed() && canvasChangeFlag) {
                            //如果发生修改动作才进行覆盖保存
                            canvasChangeFlag = false;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
                                    pixelColorRedoStack.clear();
                                    litePalCanvas.setJsonData(new Gson().toJson(PixelApp.pixelColor));
                                    litePalCanvas.setThumbnail(ParameterUtils.bitmapToBytes(loadBitmapFromView(pixelCanvas)));
                                    litePalCanvas.save();
                                }
                            }).start();
                        }
                        break;
                }
                return true;
            }
        };
        pixelCanvas.setOnTouchListener(pencilOnTouchListener);
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
        pixelColorRedoStack = new Stack<>();
        pixelColorUndoStack = new Stack<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
            }
        }).start();

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
        pixelFramelayout = findViewById(R.id.paint_framelayout);
        pencil = findViewById(R.id.draw_pencil);
        borderIndicator = findViewById(R.id.border);
        dotButton = findViewById(R.id.dot_button);
        fab = findViewById(R.id.paint_activity_fab);
        toolbar = findViewById(R.id.paint_toolbar);
        navigationView = findViewById(R.id.paint_nav);
        drawerLayout = findViewById(R.id.paint_drawer);
        screenLayout = findViewById(R.id.paint_screen);
        moveUpView = findViewById(R.id.paint_move_up);
        moveDownView = findViewById(R.id.paint_move_down);
        moveLeftView = findViewById(R.id.paint_move_left);
        moveRightView = findViewById(R.id.paint_move_right);

        //设置自定义Toolbar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    //Toolbar菜单项方法

    //清除画布
    private void clearCanvas() {
        globalChangeFlag = true;
        PixelApp.pixelColor = new int[pixelCount][pixelCount];
        new Thread(new Runnable() {
            @Override
            public void run() {
                pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
                pixelColorRedoStack.clear();
            }
        }).start();
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
        if (!eraserStatus) {
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
        } else {
            Toast.makeText(PaintActivity.this, "请先将橡皮擦切换为画笔", Toast.LENGTH_SHORT).show();
        }
    }

    //油漆桶
    private void paintBucket() {
        if (!eraserStatus) {
            globalChangeFlag = true;
            int left = pencil.getLeft();
            int bottom = pencil.getBottom();
            int x = left / pixelSize;
            int y = bottom / pixelSize;
            if (x >= pixelCount)
                x = pixelCount - 1;
            if (y >= pixelCount)
                y = pixelCount - 1;
            //获取该点颜色
            //PixelApp.pixelColor[y][x];
            int oldColor = PixelApp.pixelColor[y][x];
            int newColor = pencilColor;
            Stack<Integer> stackRow = new Stack<>();
            Stack<Integer> stackColumn = new Stack<>();
            stackRow.push(y);
            stackColumn.push(x);
            while (!stackRow.empty()) {
                int row = stackRow.pop();
                int col = stackColumn.pop();
                if (row >= 0 && row < PixelApp.pixelColor.length && col >= 0 && col < PixelApp.pixelColor.length && PixelApp.pixelColor[row][col] == oldColor && PixelApp.pixelColor[row][col] != newColor) {
                    PixelApp.pixelColor[row][col] = newColor;
                    stackRow.push(row - 1);
                    stackColumn.push(col);
                    stackRow.push(row + 1);
                    stackColumn.push(col);
                    stackRow.push(row);
                    stackColumn.push(col - 1);
                    stackRow.push(row);
                    stackColumn.push(col + 1);
                }
            }
            pixelCanvas.reDrawCanvas();
            //保存入栈
            new Thread(new Runnable() {
                @Override
                public void run() {
                    pixelColorUndoStack.push(getPixelColor(PixelApp.pixelColor));
                    pixelColorRedoStack.clear();
                }
            }).start();
        } else {
            Toast.makeText(PaintActivity.this, "请先将橡皮擦切换为画笔", Toast.LENGTH_SHORT).show();
        }
    }

    //切换显示线条
    private void toggleLine() {
        if (lineCanvas.getVisibility() == View.INVISIBLE) {
            lineCanvas.setVisibility(View.VISIBLE);
        } else {
            lineCanvas.setVisibility(View.INVISIBLE);
        }
    }

    //移动画布
    private void moveCanvas() {
        if (moveUpView.getVisibility() == View.INVISIBLE) {
            moveUpView.setVisibility(View.VISIBLE);
            moveDownView.setVisibility(View.VISIBLE);
            moveLeftView.setVisibility(View.VISIBLE);
            moveRightView.setVisibility(View.VISIBLE);
        } else {
            moveUpView.setVisibility(View.INVISIBLE);
            moveDownView.setVisibility(View.INVISIBLE);
            moveLeftView.setVisibility(View.INVISIBLE);
            moveRightView.setVisibility(View.INVISIBLE);
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
            litePalCanvas.setCreatorID(BmobUser.getCurrentUser(PixelUser.class).getObjectId());
            litePalCanvas.setCreatedAt(dateFormat.format(date));
            litePalCanvas.setUpdatedAt(dateFormat.format(date));
            litePalCanvas.setJsonData(new Gson().toJson(PixelApp.pixelColor));
            litePalCanvas.save();
        }
    }

    //分享功能，考虑生成多种格式分享
    private void shareCanvas() {
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), loadBitmapFromView(pixelFramelayout), null, null));
        Intent imageIntent = new Intent(Intent.ACTION_SEND);
        imageIntent.setType("image/*");
        imageIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(imageIntent, "分享"));
    }

    //导出对话框
    private void showExportDialog() {
        ExportDialogFragment fragment = new ExportDialogFragment();
        fragment.initParameter(litePalCanvas, pixelFramelayout);
        fragment.show(getSupportFragmentManager(), "ExportDialog");
    }

    private void requestExportPermissions() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意，执行操作
                showExportDialog();
            } else {
                //用户不同意，向用户展示该权限作用
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setMessage("应用需要读取和写入外部存储来导出图片，否则部分功能可能无法使用")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestExportPermissions();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .create()
                            .show();
                }
            }
        }
    }

    //重命名对话框
    private void showRenameDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(PaintActivity.this);
        View view = View.inflate(PaintActivity.this, R.layout.rename_dialog, null);
        final EditText renameText = view.findViewById(R.id.rename_text);
        renameText.setText(litePalCanvas.getCanvasName());
        renameText.selectAll();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                renameText.requestFocus();
                InputMethodManager inputManager =
                        (InputMethodManager) renameText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(renameText, 0);
            }
        }, 300);
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

    //撤销操作
    private void undoCanvas() {
        //栈底元素保持为初始画布信息
        //栈顶保存当前画布信息
        //通过弹出栈顶元素实现恢复操作
        //与恢复栈合作实现
        if (pixelColorUndoStack.size() > 1) {
            pixelColorRedoStack.push(pixelColorUndoStack.pop());
            PixelApp.pixelColor = getPixelColor(pixelColorUndoStack.peek());
            pixelCanvas.reDrawCanvas();
        } else {
            PixelApp.pixelColor = pixelColorUndoStack.peek();
            pixelCanvas.reDrawCanvas();
        }
    }

    //恢复操作
    private void redoCanvas() {
        //栈顶保存当前画布信息
        //通过弹出栈顶元素实现恢复操作
        //与撤销栈合作实现
        if (!pixelColorRedoStack.empty()) {
            PixelApp.pixelColor = pixelColorRedoStack.peek();
            pixelColorUndoStack.push(pixelColorRedoStack.pop());
            pixelCanvas.reDrawCanvas();
        }
    }

    //获取当前颜色二维数组的新复制对象
    private int[][] getPixelColor(int[][] src) {
        int[][] newPixelColor = new int[pixelCount][pixelCount];
        for (int i = 0; i < src.length; i++) {
            System.arraycopy(src[i], 0, newPixelColor[i], 0, src.length);
        }
        return newPixelColor;
    }

    //返回前处理
    private void backProcess() {
        //如发生修改，才更新作品信息，如更新时间，颜色等等
        if (globalChangeFlag) {
            DateFormat dateFormat = DateFormat.getDateTimeInstance();
            Date date = new Date(System.currentTimeMillis());
            litePalCanvas.setUpdatedAt(dateFormat.format(date));
        }
        //创建后未作出任何修改直接返回
        //不及时保存画布数据可能造成空指针异常
        litePalCanvas.setJsonData(new Gson().toJson(PixelApp.pixelColor));
        litePalCanvas.setThumbnail(ParameterUtils.bitmapToBytes(loadBitmapFromView(pixelCanvas)));
        litePalCanvas.save();
        PixelApp.pixelColor = null;
        PixelApp.litePalCanvas = null;
        finish();
    }

    private void changeShape() {
        switch (pencilShape) {
            case SHAPE_DOT:
                pencilShape = SHAPE_RECT;
                mMenu.findItem(R.id.pencil_shape).setIcon(R.drawable.ic_shape_rect);
                break;
//            case SHAPE_LINE:
//                pencilShape = SHAPE_RECT;
//                mMenu.findItem(R.id.pencil_shape).setIcon(R.drawable.ic_shape_rect);
//                break;
            case SHAPE_RECT:
                pencilShape = SHAPE_RECT_FILLED;
                mMenu.findItem(R.id.pencil_shape).setIcon(R.drawable.ic_shape_rect_fill);
                break;
            case SHAPE_RECT_FILLED:
                pencilShape = SHAPE_DOT;
                mMenu.findItem(R.id.pencil_shape).setIcon(R.drawable.ic_shape_dot);
                break;
        }
    }

    @Override
    //按下返回按钮
    public void onBackPressed() {
        backProcess();
    }

    @Override
    // 使得Toolbar的溢出菜单可显示图标
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }
}