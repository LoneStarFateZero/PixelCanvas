package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.sackcentury.shinebuttonlib.ShineButton;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.adapter.CommentAdapter;
import pers.lonestar.pixelcanvas.dialog.CommentDialogFragment;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.infostore.CanvasComment;
import pers.lonestar.pixelcanvas.infostore.CanvasFavorite;
import pers.lonestar.pixelcanvas.infostore.CanvasLike;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.listener.CommentInsertListener;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class CanvasInfoActivity extends BaseSwipeBackActivity {
    private static CanvasInfoActivity instance;
    private BmobCanvas bmobCanvas;
    private Toolbar toolbar;
    private PixelUser pixelUser;
    private ImageView thumbnail;
    private ImageView avatar;
    private TextView nickName;
    private TextView canvasName;
    private TextView likeCount;
    private ShineButton likeButton;
    private TextView favoriteCount;
    private ShineButton favoriteButton;
    private ImageView commentButton;
    private TextView noComment;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CommentAdapter adapter;
    private List<CanvasComment> commentList;
    private String canvasLikeId;
    private int canvasLikeCount;
    private String canvasFavoriteId;
    private int canvasFavoriteCount;

    public static CanvasInfoActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas_info);

        instance = this;
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        bmobCanvas = (BmobCanvas) intent.getSerializableExtra("pixel_canvas");
        pixelUser = (PixelUser) intent.getSerializableExtra("pixel_user");

        loadLike();
        loadFavorite();
        initListener();
        loadCanvasInfo();
        loadComment();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void initView() {
        thumbnail = (ImageView) findViewById(R.id.canvas_info_thumbnail);
        avatar = (ImageView) findViewById(R.id.canvas_info_avatar);
        nickName = (TextView) findViewById(R.id.canvas_info_nickname);
        canvasName = (TextView) findViewById(R.id.canvas_info_canvas_name);
        likeCount = (TextView) findViewById(R.id.canvas_info_like_count);
        likeButton = (ShineButton) findViewById(R.id.canvas_info_like_button);
        favoriteCount = (TextView) findViewById(R.id.canvas_info_favorite_count);
        favoriteButton = (ShineButton) findViewById(R.id.canvas_info_favorite_button);
        commentButton = (ImageView) findViewById(R.id.canvas_info_comment);
        noComment = (TextView) findViewById(R.id.canvas_info_nocomment);
        recyclerView = (RecyclerView) findViewById(R.id.canvas_info_RecyclerView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.canvas_info_SwipeRefreshLayout);
        toolbar = (Toolbar) findViewById(R.id.canvas_info_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void loadCanvasInfo() {
        Glide.with(this)
                .load(ParameterUtils.bytesToBitmap(bmobCanvas.getThumbnail()))
                .into(thumbnail);
        Glide.with(this)
                .load(pixelUser.getAvatarUrl())
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(avatar);
        nickName.setText(pixelUser.getNickname());
        canvasName.setText(bmobCanvas.getCanvasName());
    }

    private void initListener() {
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点赞
                if (likeButton.isChecked()) {
                    PixelUser currentUser = BmobUser.getCurrentUser(PixelUser.class);
                    CanvasLike canvasLike = new CanvasLike();
                    canvasLike.setCanvas(bmobCanvas);
                    canvasLike.setLikeUser(currentUser);
                    canvasLike.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                canvasLikeId = s;
                                ++canvasLikeCount;
                                likeCount.setText("+" + canvasLikeCount + " 赞");
                            } else {
                                likeButton.setChecked(false);
                            }
                        }
                    });
                }
                //取消点赞
                else {
                    CanvasLike canvasLike = new CanvasLike();
                    canvasLike.delete(canvasLikeId, new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                --canvasLikeCount;
                                likeCount.setText("+" + canvasLikeCount + " 赞");
                            } else {
                                likeButton.setChecked(true);
                            }
                        }
                    });
                }
            }
        });

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //收藏
                if (favoriteButton.isChecked()) {
                    PixelUser currentUser = BmobUser.getCurrentUser(PixelUser.class);
                    CanvasFavorite canvasFavorite = new CanvasFavorite();
                    canvasFavorite.setCanvas(bmobCanvas);
                    canvasFavorite.setCreator(pixelUser);
                    canvasFavorite.setFavoriteUser(currentUser);
                    canvasFavorite.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                canvasFavoriteId = s;
                                ++canvasFavoriteCount;
                                favoriteCount.setText("+" + canvasFavoriteCount + " 收藏");
                            } else {
                                favoriteButton.setChecked(false);
                            }
                        }
                    });
                }
                //取消收藏
                else {
                    CanvasFavorite canvasFavorite = new CanvasFavorite();
                    canvasFavorite.delete(canvasFavoriteId, new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                --canvasFavoriteCount;
                                favoriteCount.setText("+" + canvasFavoriteCount + " 收藏");
                            } else {
                                favoriteButton.setChecked(true);
                            }
                        }
                    });
                }
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentDialogFragment commentDialogFragment = new CommentDialogFragment();
                commentDialogFragment.initParameter(bmobCanvas, new CommentInsertListener() {
                    @Override
                    public void insertComment(CanvasComment canvasComment) {
                        commentList.add(0, canvasComment);
                        //没评论到有第一条评论
                        //可见性应该设置一下
                        if (!commentList.isEmpty()) {
                            noComment.setVisibility(View.GONE);
                            swipeRefreshLayout.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
                commentDialogFragment.show(getSupportFragmentManager(), "CommentDialog");
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadComment();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        adapter = new CommentAdapter(commentList);
        recyclerView.setAdapter(adapter);

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击头像跳转到个人主页
                Intent intent = new Intent(CanvasInfoActivity.getInstance(), ProfileActivity.class);
                intent.putExtra("pixel_user", pixelUser);
                CanvasInfoActivity.getInstance().startActivity(intent);
            }
        });
    }

    private void loadComment() {
        BmobQuery<CanvasComment> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("canvas", bmobCanvas);
        bmobQuery.order("-createdAt");
        bmobQuery.include("canvas");
        bmobQuery.include("commentUser");
        bmobQuery.findObjects(new FindListener<CanvasComment>() {
            @Override
            public void done(List<CanvasComment> list, BmobException e) {
                if (e == null) {
                    if (list.isEmpty()) {
                        noComment.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setVisibility(View.GONE);
                    } else {
                        noComment.setVisibility(View.GONE);
                        swipeRefreshLayout.setVisibility(View.VISIBLE);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    commentList.clear();
                    commentList.addAll(list);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CanvasInfoActivity.this, "评论加载失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadLike() {
        BmobQuery<CanvasLike> bmobQuery = new BmobQuery<>();
        PixelUser currentUser = BmobUser.getCurrentUser(PixelUser.class);
        bmobQuery.addWhereEqualTo("canvas", bmobCanvas);
        bmobQuery.addWhereEqualTo("likeUser", currentUser);
        bmobQuery.findObjects(new FindListener<CanvasLike>() {
            @Override
            public void done(List<CanvasLike> list, BmobException e) {
                if (e == null) {
                    if (list.isEmpty()) {
                        likeButton.setChecked(false);
                    } else {
                        likeButton.setChecked(true);
                        canvasLikeId = list.get(0).getObjectId();
                    }
                } else {
                    likeButton.setChecked(false);
                }
            }
        });

        bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("canvas", bmobCanvas);
        bmobQuery.count(CanvasLike.class, new CountListener() {
            @Override
            public void done(Integer integer, BmobException e) {
                if (e == null) {
                    canvasLikeCount = integer;
                    likeCount.setText("+" + canvasLikeCount + " 赞");
                } else {
                    likeCount.setText("+0 赞");
                }
            }
        });
    }

    private void loadFavorite() {
        BmobQuery<CanvasFavorite> bmobFavoriteQuery = new BmobQuery<>();
        PixelUser currentUser = BmobUser.getCurrentUser(PixelUser.class);
        bmobFavoriteQuery.addWhereEqualTo("canvas", bmobCanvas);
        bmobFavoriteQuery.addWhereEqualTo("favoriteUser", currentUser);
        bmobFavoriteQuery.findObjects(new FindListener<CanvasFavorite>() {
            @Override
            public void done(List<CanvasFavorite> list, BmobException e) {
                if (e == null) {
                    if (list.isEmpty()) {
                        favoriteButton.setChecked(false);
                    } else {
                        favoriteButton.setChecked(true);
                        canvasFavoriteId = list.get(0).getObjectId();
                    }
                } else {
                    favoriteButton.setChecked(false);
                }
            }
        });

        bmobFavoriteQuery = new BmobQuery<>();
        bmobFavoriteQuery.addWhereEqualTo("canvas", bmobCanvas);
        bmobFavoriteQuery.count(CanvasFavorite.class, new CountListener() {
            @Override
            public void done(Integer integer, BmobException e) {
                if (e == null) {
                    canvasFavoriteCount = integer;
                    favoriteCount.setText("+" + canvasFavoriteCount + " 收藏");
                } else {
                    favoriteCount.setText("+0 收藏");
                }
            }
        });
    }

    private void downloadCanvas() {
        LitePalCanvas litePalCanvas = new LitePalCanvas();
        litePalCanvas.setCanvasName(bmobCanvas.getCanvasName() + "-下载");
        litePalCanvas.setCreatorID(bmobCanvas.getCreator().getObjectId());
        litePalCanvas.setPixelCount(bmobCanvas.getPixelCount());
        litePalCanvas.setJsonData(bmobCanvas.getJsonData());
        litePalCanvas.setCreatedAt(bmobCanvas.getCreatedAt());
        litePalCanvas.setUpdatedAt(bmobCanvas.getUpdatedAt());
        litePalCanvas.setThumbnail(bmobCanvas.getThumbnail());
        litePalCanvas.save();
        Toast.makeText(this, "已下载到本地", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.download:
                //下载作品到本地
                downloadCanvas();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    //Toolbar菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.canvas_info_menu, menu);
        return true;
    }
}
