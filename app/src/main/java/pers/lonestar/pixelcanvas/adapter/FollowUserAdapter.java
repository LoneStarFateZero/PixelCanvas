package pers.lonestar.pixelcanvas.adapter;

import android.content.Intent;
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
import pers.lonestar.pixelcanvas.activity.FollowActivity;
import pers.lonestar.pixelcanvas.activity.ProfileActivity;
import pers.lonestar.pixelcanvas.infostore.PixelUser;

public class FollowUserAdapter extends RecyclerView.Adapter<FollowUserAdapter.ViewHolder> {
    private List<PixelUser> pixelUserList;

    public FollowUserAdapter(List<PixelUser> pixelUserList) {
        this.pixelUserList = pixelUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final PixelUser pixelUser = pixelUserList.get(position);
        Glide.with(FollowActivity.getInstance()).load(pixelUser.getAvatarUrl()).into(holder.avatar);
        holder.nickName.setText(pixelUser.getNickname());
        holder.userItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击转到个人主页
                Intent intent = new Intent(FollowActivity.getInstance(), ProfileActivity.class);
                intent.putExtra("pixel_user", pixelUser);
                FollowActivity.getInstance().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pixelUserList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View userItem;
        CircleImageView avatar;
        TextView nickName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userItem = itemView;
            avatar = itemView.findViewById(R.id.follow_user_item_avatar);
            nickName = itemView.findViewById(R.id.follow_user_item_nickname);
        }
    }
}
