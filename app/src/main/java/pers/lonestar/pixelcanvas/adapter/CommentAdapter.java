package pers.lonestar.pixelcanvas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.CanvasInfoActivity;
import pers.lonestar.pixelcanvas.infostore.CanvasComment;

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
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        CanvasComment canvasComment = commentList.get(position);
        Glide.with(CanvasInfoActivity.getInstance()).load(canvasComment.getCommentUser().getAvatarUrl()).into(holder.avatar);
        holder.nickName.setText(canvasComment.getCommentUser().getNickname());
        holder.commentTime.setText(canvasComment.getCreatedAt());
        holder.commentContent.setText(canvasComment.getCommentText());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView nickName;
        TextView commentTime;
        TextView commentContent;

        RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.comment_item_avatar);
            nickName = itemView.findViewById(R.id.comment_item_nickname);
            commentTime = itemView.findViewById(R.id.comment_item_time);
            commentContent = itemView.findViewById(R.id.comment_item_content);
        }
    }
}
