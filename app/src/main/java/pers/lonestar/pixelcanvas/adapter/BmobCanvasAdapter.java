package pers.lonestar.pixelcanvas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class BmobCanvasAdapter extends RecyclerView.Adapter<BmobCanvasAdapter.ViewHolder> {
    private List<BmobCanvas> bmobCanvasList;
    // 普通布局
    private final int TYPE_ITEM = 1;
    // 脚布局
    private final int TYPE_FOOTER = 2;
    // 当前加载状态，默认为加载完成
    private int loadState = 2;
    // 正在加载
    public final int LOADING = 1;
    // 加载完成
    public final int LOADING_COMPLETE = 2;
    // 加载到底
    public final int LOADING_END = 3;

    public BmobCanvasAdapter(List<BmobCanvas> bmobCanvasList) {
        this.bmobCanvasList = bmobCanvasList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_canvas_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final BmobCanvas bmobCanvas = bmobCanvasList.get(position);
        holder.thumbnail.setImageBitmap(ParameterUtils.bytesToBitmap(bmobCanvas.getThumbnail()));
        holder.canvasName.setText(bmobCanvas.getCanvasName());
        holder.canvasUpdated.setText(bmobCanvas.getUpdatedAt());
        holder.canvasItemView.setOnClickListener(new View.OnClickListener() {
            //点击转到对应作品主页
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return bmobCanvasList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View canvasItemView;
        ImageView thumbnail;
        TextView canvasName;
        TextView canvasUpdated;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            canvasItemView = itemView;
            thumbnail = itemView.findViewById(R.id.post_canvas_item_thumbnail);
            canvasName = itemView.findViewById(R.id.post_canvas_item_name);
            canvasUpdated = itemView.findViewById(R.id.post_canvas_item_time);
        }
    }
}