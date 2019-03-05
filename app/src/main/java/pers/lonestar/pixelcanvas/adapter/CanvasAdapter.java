package pers.lonestar.pixelcanvas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import pers.lonestar.pixelcanvas.R;
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
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LitePalCanvas litePalCanvas = litePalCanvasList.get(position);
        holder.thumbnail.setImageBitmap(ParameterUtils.bytesToBitmap(litePalCanvas.getThumbnail()));
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
        ImageView thumbnail;
        TextView canvasName;
        TextView canvasSize;
        TextView canvasUpdated;
        ImageView canvasMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.item_canvas_image);
            canvasName = itemView.findViewById(R.id.item_canvas_name);
            canvasSize = itemView.findViewById(R.id.item_canvas_size);
            canvasUpdated = itemView.findViewById(R.id.item_canvas_updated);
            canvasMenu = itemView.findViewById(R.id.item_menu);
        }
    }
}
