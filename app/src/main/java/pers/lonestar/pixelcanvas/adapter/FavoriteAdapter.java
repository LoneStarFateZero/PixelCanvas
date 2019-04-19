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
import pers.lonestar.pixelcanvas.activity.CanvasInfoActivity;
import pers.lonestar.pixelcanvas.activity.FavoriteActivity;
import pers.lonestar.pixelcanvas.activity.ProfileActivity;
import pers.lonestar.pixelcanvas.infostore.CanvasFavorite;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class FavoriteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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

    private List<CanvasFavorite> canvasFavoriteList;

    public FavoriteAdapter(List<CanvasFavorite> canvasFavoriteList) {
        super();
        this.canvasFavoriteList = canvasFavoriteList;
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_canvas_item, parent, false);
            return new FavoriteAdapter.RecyclerViewHolder(view);
        }
        //添加FootView
        else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footview, parent, false);
            return new FavoriteAdapter.FootViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FavoriteAdapter.RecyclerViewHolder) {
            final FavoriteAdapter.RecyclerViewHolder recyclerViewHolder = (FavoriteAdapter.RecyclerViewHolder) holder;
            final CanvasFavorite canvasFavorite = canvasFavoriteList.get(position);
            //作品名称
            recyclerViewHolder.canvasName.setText(canvasFavorite.getCanvas().getCanvasName());
            //设置当前昵称
            recyclerViewHolder.nickName.setText(canvasFavorite.getCreator().getNickname());
            //设置发布时间
            recyclerViewHolder.createdTime.setText(canvasFavorite.getCanvas().getCreatedAt());
            //缩略图
            Glide.with(FavoriteActivity.getInstance()).load(ParameterUtils.bytesToBitmap(canvasFavorite.getCanvas().getThumbnail())).into(recyclerViewHolder.thumbnail);
            //设置当前头像
            Glide.with(FavoriteActivity.getInstance())
                    .load(canvasFavorite.getCreator().getAvatarUrl())
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(recyclerViewHolder.avatar);
            //点击头像转到个人主页
            recyclerViewHolder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FavoriteActivity.getInstance(), ProfileActivity.class);
                    intent.putExtra("pixel_user", canvasFavorite.getCreator());
                    FavoriteActivity.getInstance().startActivity(intent);
                }
            });
            //点击缩略图转到作品主页
            recyclerViewHolder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FavoriteActivity.getInstance(), CanvasInfoActivity.class);
                    intent.putExtra("pixel_canvas", canvasFavorite.getCanvas());
                    intent.putExtra("pixel_user", canvasFavorite.getCreator());
                    FavoriteActivity.getInstance().startActivity(intent);
                }
            });
        } else if (holder instanceof FavoriteAdapter.FootViewHolder) {
            FavoriteAdapter.FootViewHolder footViewHolder = (FavoriteAdapter.FootViewHolder) holder;
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
        return canvasFavoriteList.size() + 1;
    }

    /**
     * 设置上拉加载状态
     *
     * @param loadState 0.正在加载 1.加载完成 2.加载到底
     */
    public void setLoadState(int loadState) {
        if (this.loadState != LOADING_END) {
            this.loadState = loadState;
            notifyItemChanged(canvasFavoriteList.size());
        }
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView nickName;
        TextView createdTime;
        TextView canvasName;
        ImageView thumbnail;

        RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.follow_canvas_avatar);
            nickName = itemView.findViewById(R.id.follow_canvas_nickname);
            createdTime = itemView.findViewById(R.id.follow_canvas_time);
            canvasName = itemView.findViewById(R.id.follow_canvas_name);
            thumbnail = itemView.findViewById(R.id.follow_canvas_thumbnail);
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
