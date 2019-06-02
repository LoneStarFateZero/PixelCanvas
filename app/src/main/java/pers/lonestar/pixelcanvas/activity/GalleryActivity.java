package pers.lonestar.pixelcanvas.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.adapter.LocalCanvasAdapter;
import pers.lonestar.pixelcanvas.dialog.NewCanvasDialogFragment;
import pers.lonestar.pixelcanvas.infostore.FileCanvas;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;
import pers.lonestar.pixelcanvas.infostore.ShowCheckBox;
import pers.lonestar.pixelcanvas.utils.FileUtils;

public class GalleryActivity extends BaseSwipeBackActivity {
    private FloatingActionButton fab;
    private NewCanvasDialogFragment fragment;
    private RecyclerView recyclerView;
    private RelativeLayout bottomChoose;
    private Button chooseAllButton;
    private Button reverseChooseAllButton;
    private Button deleteButton;
    private Toolbar toolbar;
    private List<LitePalCanvas> litePalCanvasList;
    private List<Boolean> litePalCanvasChooseList;
    private ShowCheckBox showCheckBox;
    private LocalCanvasAdapter localCanvasAdapter;
    private static GalleryActivity instance;

    public static GalleryActivity getInstance() {
        return instance;
    }

    //onStart()似乎不会在PaintActivity返回时被调用，此处采用onResume()方法来刷新数据
    @Override
    protected void onResume() {
        super.onResume();
        initCanvasList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        instance = this;
        showCheckBox = new ShowCheckBox();
        showCheckBox.setShowCheckBox(false);

        initView();
        initListener();
    }

    private void initListener() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment = new NewCanvasDialogFragment();
                fragment.show(getSupportFragmentManager(), "NewCanvasDialog");
            }
        });
        reverseChooseAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < litePalCanvasChooseList.size(); i++) {
                    litePalCanvasChooseList.set(i, !litePalCanvasChooseList.get(i));
                }
                localCanvasAdapter.notifyDataSetChanged();
            }
        });
        chooseAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < litePalCanvasChooseList.size(); i++) {
                    litePalCanvasChooseList.set(i, true);
                }
                localCanvasAdapter.notifyDataSetChanged();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(GalleryActivity.this);
                dialog.setMessage("是否删除选中作品？\n（操作不可恢复）")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<LitePalCanvas> newLitePalCanvasList = new ArrayList<>();
                                for (int i = 0; i < litePalCanvasChooseList.size(); i++) {
                                    if (!litePalCanvasChooseList.get(i)) {
                                        newLitePalCanvasList.add(litePalCanvasList.get(i));
                                    }
                                    //已选择，删除
                                    else {
                                        litePalCanvasList.get(i).delete();
                                    }
                                }
                                litePalCanvasChooseList.clear();
                                litePalCanvasList.clear();
                                litePalCanvasList.addAll(newLitePalCanvasList);
                                showCheckBox.setShowCheckBox(false);
                                fab.setVisibility(View.VISIBLE);
                                bottomChoose.setVisibility(View.GONE);
                                localCanvasAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(true)
                        .show();
            }
        });

        litePalCanvasList = new ArrayList<>();
        litePalCanvasChooseList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        localCanvasAdapter = new LocalCanvasAdapter(litePalCanvasList, litePalCanvasChooseList, showCheckBox);
        recyclerView.setAdapter(localCanvasAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        Glide.with(GalleryActivity.this).resumeRequests();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        Glide.with(GalleryActivity.this).pauseRequests();
                        break;
                }
            }
        });
    }

    private void initView() {
        fab = (FloatingActionButton) findViewById(R.id.gallery_activity_fab);
        recyclerView = (RecyclerView) findViewById(R.id.gallery_activity_recyclerview);
        bottomChoose = (RelativeLayout) findViewById(R.id.gallery_activity_bottom_choose);
        chooseAllButton = (Button) findViewById(R.id.gallery_activity_botton_chooseall);
        reverseChooseAllButton = (Button) findViewById(R.id.gallery_activity_botton_reverse_chooseall);
        deleteButton = (Button) findViewById(R.id.gallery_activity_botton_delete);
        toolbar = (Toolbar) findViewById(R.id.gallery_activity_toolbar);
        toolbar.setTitle("本地作品");
        //设置标题字体样式为像素字体，否则为默认字体，与整体像素风格不匹配
        toolbar.setTitleTextAppearance(this, R.style.TitleStyle);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void initCanvasList() {
        //初始化本地作品列表
        showCheckBox.setShowCheckBox(false);
        litePalCanvasList.clear();
        litePalCanvasList.addAll(LitePal.order("id desc").find(LitePalCanvas.class));
        localCanvasAdapter.notifyDataSetChanged();
    }

    private void importLocalFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //设置类型
        intent.setType("file/*.pixel");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            if (uri != null) {
                try {
                    File file = new File(FileUtils.getFilePathByUri(this, uri));
                    ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
                    FileCanvas fileCanvas = (FileCanvas) objectInputStream.readObject();
                    objectInputStream.close();
                    LitePalCanvas litePalCanvas = new LitePalCanvas();
                    litePalCanvas.setCanvasName(fileCanvas.getCanvasName());
                    litePalCanvas.setCreatedAt(fileCanvas.getCreatedAt());
                    litePalCanvas.setUpdatedAt(fileCanvas.getUpdatedAt());
                    litePalCanvas.setCreatorID(fileCanvas.getCreatorID());
                    litePalCanvas.setPixelCount(fileCanvas.getPixelCount());
                    litePalCanvas.setJsonData(fileCanvas.getJsonData());
                    litePalCanvas.setThumbnail(fileCanvas.getThumbnail());
                    litePalCanvas.save();
                    litePalCanvasList.add(0, litePalCanvas);
                    localCanvasAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "作品已导入", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this, "作品文件读取错误，请检查设置", Toast.LENGTH_LONG).show();
                } catch (ClassNotFoundException e) {
                    Toast.makeText(this, "作品文件格式不正确，请检查是否为canvas文件", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //是否显示选择框
    private void showCheckBox() {
        if (showCheckBox.isShowCheckBox()) {
            showCheckBox.setShowCheckBox(false);
            fab.setVisibility(View.VISIBLE);
            bottomChoose.setVisibility(View.GONE);
        } else {
            showCheckBox.setShowCheckBox(true);
            fab.setVisibility(View.GONE);
            bottomChoose.setVisibility(View.VISIBLE);
            litePalCanvasChooseList.clear();
            for (int i = 0; i < litePalCanvasList.size(); i++) {
                litePalCanvasChooseList.add(false);
            }
        }
        localCanvasAdapter.notifyDataSetChanged();
    }

    //Toolbar菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.import_file:
                importLocalFile();
                break;
            case R.id.edit_gallery:
                showCheckBox();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (showCheckBox.isShowCheckBox()) {
            showCheckBox.setShowCheckBox(false);
            fab.setVisibility(View.VISIBLE);
            bottomChoose.setVisibility(View.GONE);
            localCanvasAdapter.notifyDataSetChanged();
        } else
            finish();
    }
}
