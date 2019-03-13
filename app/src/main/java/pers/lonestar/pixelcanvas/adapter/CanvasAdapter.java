package pers.lonestar.pixelcanvas.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.GalleryActivity;
import pers.lonestar.pixelcanvas.activity.PaintActivity;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class CanvasAdapter extends RecyclerView.Adapter<CanvasAdapter.ViewHolder> {
    private List<LitePalCanvas> litePalCanvasList;

    public CanvasAdapter(List<LitePalCanvas> litePalCanvasList) {
        this.litePalCanvasList = litePalCanvasList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.canvas_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.canvasItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GalleryActivity.getInstance(), PaintActivity.class);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LitePalCanvas litePalCanvas = litePalCanvasList.get(position);
        //图片加载或许需要优化
        Glide.with(GalleryActivity.getInstance()).load(ParameterUtils.bytesToBitmap(litePalCanvas.getThumbnail())).into(holder.thumbnail);
//        holder.thumbnail.setImageBitmap(ParameterUtils.bytesToBitmap(litePalCanvas.getThumbnail()));
        holder.canvasName.setText(litePalCanvas.getCanvasName());
        holder.canvasSize.setText("Size:" + litePalCanvas.getPixelCount() + "x" + litePalCanvas.getPixelCount());
        holder.canvasUpdated.setText(litePalCanvas.getUpdatedAt());
        holder.canvasMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "测试-弹出悬浮窗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return litePalCanvasList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View canvasItemView;
        ImageView thumbnail;
        TextView canvasName;
        TextView canvasSize;
        TextView canvasUpdated;
        ImageView canvasMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            canvasItemView = itemView;
            thumbnail = itemView.findViewById(R.id.item_canvas_image);
            canvasName = itemView.findViewById(R.id.item_canvas_name);
            canvasSize = itemView.findViewById(R.id.item_canvas_size);
            canvasUpdated = itemView.findViewById(R.id.item_canvas_updated);
            canvasMenu = itemView.findViewById(R.id.item_menu);
        }
    }
}
