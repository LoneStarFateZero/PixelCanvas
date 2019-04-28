package pers.lonestar.pixelcanvas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.PaintActivity;

public class ColorAdater extends RecyclerView.Adapter<ColorAdater.ViewHolder> {
    private List<Integer> pencilColorList;

    public ColorAdater(List<Integer> pencilColorList) {
        this.pencilColorList = pencilColorList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.colorGridItem.setBackgroundColor(pencilColorList.get(position));
        holder.colorGridItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PaintActivity.getInstance().setPencilColor(pencilColorList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return pencilColorList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View colorGridItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorGridItem = itemView.findViewById(R.id.color_item);
        }
    }
}
