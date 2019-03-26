package pers.lonestar.pixelcanvas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class BmobCanvasAdapter extends RecyclerView.Adapter<BmobCanvasAdapter.ViewHolder> {
    private List<BmobCanvas> bmobCanvasList;

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
        holder.likeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //喜欢数+1
                //按钮动画效果
            }
        });
    }

    @Override
    public int getItemCount() {
        if (bmobCanvasList == null)
            return 0;
        return bmobCanvasList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View canvasItemView;
        ImageView thumbnail;
        TextView canvasName;
        TextView canvasUpdated;
        FloatingActionButton likeFab;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            canvasItemView = itemView;
            thumbnail = itemView.findViewById(R.id.post_canvas_item_thumbnail);
            canvasName = itemView.findViewById(R.id.post_canvas_item_name);
            canvasUpdated = itemView.findViewById(R.id.post_canvas_item_time);
            likeFab = itemView.findViewById(R.id.post_canvas_item_like);
        }
    }
}