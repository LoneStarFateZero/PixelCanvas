package pers.lonestar.pixelcanvas.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sackcentury.shinebuttonlib.ShineButton;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.adapter.CommentAdapter;
import pers.lonestar.pixelcanvas.dialog.CommentDialogFragment;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.infostore.CanvasComment;
import pers.lonestar.pixelcanvas.infostore.CanvasFavorite;
import pers.lonestar.pixelcanvas.infostore.CanvasLike;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.listener.CommentInsertListener;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class CanvasInfoActivity extends AppCompatActivity {
    private static CanvasInfoActivity instance;
    private BmobCanvas bmobCanvas;
    private PixelUser pixelUser;
    private ImageView thumbnail;
    private CircleImageView avatar;
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

        Intent intent = getIntent();
        bmobCanvas = (BmobCanvas) intent.getSerializableExtra("pixel_canvas");
        pixelUser = (PixelUser) intent.getSerializableExtra("pixel_user");

        initView();
        loadLike();
        loadFavorite();
        initListener();
        loadCanvasInfo();
        loadComment();
    }

    private void initView() {
        thumbnail = findViewById(R.id.canvas_info_thumbnail);
        avatar = findViewById(R.id.canvas_info_avatar);
        nickName = findViewById(R.id.canvas_info_nickname);
        canvasName = findViewById(R.id.canvas_info_canvas_name);
        likeCount = findViewById(R.id.canvas_info_like_count);
        likeButton = findViewById(R.id.canvas_info_like_button);
        favoriteCount = findViewById(R.id.canvas_info_favorite_count);
        favoriteButton = findViewById(R.id.canvas_info_favorite_button);
        commentButton = findViewById(R.id.canvas_info_comment);
        noComment = findViewById(R.id.canvas_info_nocomment);
        recyclerView = findViewById(R.id.canvas_info_RecyclerView);
        swipeRefreshLayout = findViewById(R.id.canvas_info_SwipeRefreshLayout);
    }

    private void loadCanvasInfo() {
        Glide.with(this).load(ParameterUtils.bytesToBitmap(bmobCanvas.getThumbnail())).into(thumbnail);
        Glide.with(this).load(pixelUser.getAvatarUrl()).into(avatar);
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
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        noComment.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
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
}
