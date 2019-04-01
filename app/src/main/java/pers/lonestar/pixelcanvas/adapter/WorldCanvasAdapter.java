package pers.lonestar.pixelcanvas.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;
import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.MainActivity;
import pers.lonestar.pixelcanvas.activity.ProfileActivity;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class WorldCanvasAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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
    private List<BmobCanvas> bmobCanvasList;
    // 当前加载状态，默认为加载完成
    private int loadState = 2;

    public WorldCanvasAdapter(List<BmobCanvas> bmobCanvasList) {
        this.bmobCanvasList = bmobCanvasList;
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.world_canvas_item, parent, false);
            return new RecyclerViewHolder(view);
        }
        //添加FootView
        else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footview, parent, false);
            return new FootViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof WorldCanvasAdapter.RecyclerViewHolder) {
            final WorldCanvasAdapter.RecyclerViewHolder recyclerViewHolder = (WorldCanvasAdapter.RecyclerViewHolder) holder;
            BmobCanvas bmobCanvas = bmobCanvasList.get(position);
            //缩略图
            Glide.with(MainActivity.getInstance()).load(ParameterUtils.bytesToBitmap(bmobCanvas.getThumbnail())).into(recyclerViewHolder.thumbnail);
            //作品名称
            recyclerViewHolder.canvasName.setText(bmobCanvas.getCanvasName());
            //还需要根据ID查询作者当前头像和当前昵称
            BmobQuery<PixelUser> bmobQuery = new BmobQuery<>();
            bmobQuery.addWhereEqualTo("objectId", bmobCanvas.getCreatorID());
            bmobQuery.findObjects(new FindListener<PixelUser>() {
                @Override
                public void done(final List<PixelUser> list, BmobException e) {
                    if (e == null) {
                        recyclerViewHolder.nickName.setText(list.get(0).getNickname());
                        String avatarUrl = list.get(0).getAvatarUrl();
                        if (avatarUrl == null)
                            Glide.with(MainActivity.getInstance()).load(PixelApp.defaultAvatarUrl).into(recyclerViewHolder.avatar);
                        else
                            Glide.with(MainActivity.getInstance()).load(list.get(0).getAvatarUrl()).into(recyclerViewHolder.avatar);
                        //点击头像转到个人主页
                        recyclerViewHolder.avatar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //TODO
//                                PixelUser pixelUser = new PixelUser();
                                Intent intent = new Intent(MainActivity.getInstance(), ProfileActivity.class);
                                intent.putExtra("pixel_user", list.get(0));
                                MainActivity.getInstance().startActivity(intent);
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.getInstance(), "用户信息获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //点击缩略图转到作品主页
            recyclerViewHolder.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        } else if (holder instanceof WorldCanvasAdapter.FootViewHolder) {
            WorldCanvasAdapter.FootViewHolder footViewHolder = (WorldCanvasAdapter.FootViewHolder) holder;
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
        return bmobCanvasList.size() + 1;
    }

    /**
     * 设置上拉加载状态
     *
     * @param loadState 0.正在加载 1.加载完成 2.加载到底
     */
    public void setLoadState(int loadState) {
        this.loadState = loadState;
        notifyItemChanged(bmobCanvasList.size());
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    // 如果当前是footer的位置，那么该item占据2个单元格，正常情况下占据1个单元格
                    return getItemViewType(position) == TYPE_FOOTER ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        CircleImageView avatar;
        TextView canvasName;
        TextView nickName;

        RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.main_canvas_avatar);
            thumbnail = itemView.findViewById(R.id.main_canvas_thumbnail);
            canvasName = itemView.findViewById(R.id.main_canvas_name);
            nickName = itemView.findViewById(R.id.main_canvas_nickname);
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
