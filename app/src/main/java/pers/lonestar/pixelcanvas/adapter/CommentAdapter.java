package pers.lonestar.pixelcanvas.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.CanvasInfoActivity;
import pers.lonestar.pixelcanvas.activity.ProfileActivity;
import pers.lonestar.pixelcanvas.infostore.CanvasComment;
import pers.lonestar.pixelcanvas.infostore.PixelUser;

import static androidx.core.content.ContextCompat.getSystemService;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.RecyclerViewHolder> {
    private List<CanvasComment> commentList;

    public CommentAdapter(List<CanvasComment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewHolder holder, final int position) {
        final CanvasComment canvasComment = commentList.get(position);
        Glide.with(CanvasInfoActivity.getInstance())
                .load(canvasComment.getCommentUser().getAvatarUrl())
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(holder.avatar);
        holder.nickName.setText(canvasComment.getCommentUser().getNickname());
        holder.commentTime.setText(canvasComment.getCreatedAt());
        holder.commentContent.setText(canvasComment.getCommentText());
        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击头像跳转到个人主页
                Intent intent = new Intent(CanvasInfoActivity.getInstance(), ProfileActivity.class);
                intent.putExtra("pixel_user", canvasComment.getCommentUser());
                CanvasInfoActivity.getInstance().startActivity(intent);
            }
        });
        //长按弹出删除菜单
        //需要根据评论用户和当前用户是否一致来决定弹出
        holder.commentView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (commentList.get(position).getCommentUser().getObjectId().equals(BmobUser.getCurrentUser(PixelUser.class).getObjectId())) {
                    new AlertDialog.Builder(CanvasInfoActivity.getInstance())
                            .setMessage("您确定要删除此条评论吗？")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    commentList.get(position).delete(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                commentList.remove(position);
                                                notifyDataSetChanged();
                                            } else {
                                                Toast.makeText(CanvasInfoActivity.getInstance(), "删除失败，请检查网络设置后重试", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }).setNegativeButton("取消", null)
                            .create()
                            .show();
                }
                //点击事件消耗，不继续传播执行短按事件
                return true;
            }
        });
        //短按复制评论
        holder.commentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取剪贴板管理器：
                ClipboardManager cm = getSystemService(CanvasInfoActivity.getInstance(), ClipboardManager.class);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("CommentContent", holder.commentContent.getText());
                // 将ClipData内容放到系统剪贴板里
                if (cm != null) {
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(CanvasInfoActivity.getInstance(), "评论已复制", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        View commentView;
        ImageView avatar;
        TextView nickName;
        TextView commentTime;
        TextView commentContent;

        RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            commentView = itemView;
            avatar = itemView.findViewById(R.id.comment_item_avatar);
            nickName = itemView.findViewById(R.id.comment_item_nickname);
            commentTime = itemView.findViewById(R.id.comment_item_time);
            commentContent = itemView.findViewById(R.id.comment_item_content);
        }
    }
}
