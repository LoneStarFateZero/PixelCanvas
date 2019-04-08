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
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.adapter.CommentAdapter;
import pers.lonestar.pixelcanvas.dialog.CommentDialogFragment;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.infostore.CanvasComment;
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
}
