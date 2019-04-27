package pers.lonestar.pixelcanvas.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.ProfileActivity;
import pers.lonestar.pixelcanvas.activity.SearchActivity;
import pers.lonestar.pixelcanvas.infostore.PixelUser;

public class SearchUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // 正在加载
    public final int LOADING = 1;
    // 加载完成
    public final int LOADING_COMPLETE = 2;
    // 加载到底
    public final int LOADING_END = 3;
    // 普通布局
    private final int TYPE_ITEM = 1;
    // 脚布局
    private final int TYPE_FOOTER = 2;
    // 当前加载状态，默认为加载完成
    private int loadState = 2;

    private List<PixelUser> pixelUserList;

    public SearchUserAdapter(List<PixelUser> pixelUserList) {
        super();
        this.pixelUserList = pixelUserList;
    }

    @Override
    public int getItemViewType(int position) {
        // 最后一个item设置为FooterView
        if (position + 1 == getItemCount())
            return TYPE_FOOTER;
        else
            return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //添加一般View
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_user_item, parent, false);
            return new SearchUserAdapter.RecyclerViewHolder(view);
        }
        //添加FootView
        else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footview, parent, false);
            return new SearchUserAdapter.FootViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SearchUserAdapter.RecyclerViewHolder) {
            final SearchUserAdapter.RecyclerViewHolder recyclerViewHolder = (SearchUserAdapter.RecyclerViewHolder) holder;
            final PixelUser pixelUser = pixelUserList.get(position);
            Glide.with(SearchActivity.getInstance())
                    .load(pixelUser.getAvatarUrl())
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(recyclerViewHolder.avatar);
            recyclerViewHolder.nickName.setText(pixelUser.getNickname());
            recyclerViewHolder.userEmail.setText(pixelUser.getUsername());
            recyclerViewHolder.userItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //点击转到个人主页
                    Intent intent = new Intent(SearchActivity.getInstance(), ProfileActivity.class);
                    intent.putExtra("pixel_user", pixelUser);
                    SearchActivity.getInstance().startActivity(intent);
                }
            });
        } else if (holder instanceof SearchUserAdapter.FootViewHolder) {
            SearchUserAdapter.FootViewHolder footViewHolder = (SearchUserAdapter.FootViewHolder) holder;
            switch (loadState) {
                case LOADING: // 正在加载
                    footViewHolder.pbLoading.setVisibility(View.VISIBLE);
                    footViewHolder.tvLoading.setVisibility(View.VISIBLE);
                    footViewHolder.llEnd.setVisibility(View.GONE);
                    break;

                case LOADING_COMPLETE: // 加载完成
                    footViewHolder.pbLoading.setVisibility(View.INVISIBLE);
                    footViewHolder.tvLoading.setVisibility(View.INVISIBLE);
                    footViewHolder.llEnd.setVisibility(View.GONE);
                    break;

                case LOADING_END: // 加载到底
                    footViewHolder.pbLoading.setVisibility(View.GONE);
                    footViewHolder.tvLoading.setVisibility(View.GONE);
                    footViewHolder.llEnd.setVisibility(View.VISIBLE);
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return pixelUserList.size() + 1;
    }

    public void setLoadState(int loadState) {
        if (this.loadState != LOADING_END) {
            this.loadState = loadState;
            notifyItemChanged(pixelUserList.size());
        }
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        View userItem;
        ImageView avatar;
        TextView nickName;
        TextView userEmail;

        RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            userItem = itemView;
            avatar = itemView.findViewById(R.id.follow_user_item_avatar);
            nickName = itemView.findViewById(R.id.follow_user_item_nickname);
            userEmail = itemView.findViewById(R.id.follow_user_item_email);
        }
    }

    private class FootViewHolder extends RecyclerView.ViewHolder {
        ProgressBar pbLoading;
        TextView tvLoading;
        LinearLayout llEnd;

        FootViewHolder(@NonNull View itemView) {
            super(itemView);
            pbLoading = itemView.findViewById(R.id.pb_loading);
            tvLoading = itemView.findViewById(R.id.tv_loading);
            llEnd = itemView.findViewById(R.id.ll_end);
        }
    }
}
