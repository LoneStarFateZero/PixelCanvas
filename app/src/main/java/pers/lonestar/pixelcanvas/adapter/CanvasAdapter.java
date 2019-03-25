package pers.lonestar.pixelcanvas.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.GalleryActivity;
import pers.lonestar.pixelcanvas.activity.PaintActivity;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
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
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final LitePalCanvas litePalCanvas = litePalCanvasList.get(position);
        //图片加载或许需要优化
        Glide.with(GalleryActivity.getInstance()).load(ParameterUtils.bytesToBitmap(litePalCanvas.getThumbnail())).into(holder.thumbnail);
//        holder.thumbnail.setImageBitmap(ParameterUtils.bytesToBitmap(litePalCanvas.getThumbnail()));
        holder.canvasName.setText(litePalCanvas.getCanvasName());
        holder.canvasSize.setText("Size:" + litePalCanvas.getPixelCount() + "x" + litePalCanvas.getPixelCount());
        holder.canvasUpdated.setText(litePalCanvas.getUpdatedAt());
        holder.canvasMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出菜单
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.canvas_list_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.list_post:
                                break;
                            case R.id.list_rename:
                                break;
                            case R.id.list_copy:
                                break;
                            case R.id.list_delete:
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
        holder.canvasItemView.setOnClickListener(new View.OnClickListener() {
            //点击转到对应编辑活动
            @Override
            public void onClick(View v) {
                PixelApp.litePalCanvas = litePalCanvas;
                Intent intent = new Intent(GalleryActivity.getInstance(), PaintActivity.class);
                GalleryActivity.getInstance().startActivity(intent);
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

    private void showPostDialog() {

    }

    private void showRenameDialog() {

    }

    private void showCopyDialog() {

    }

    private void showDeleteDialog() {

    }

    private void postCanvasFile(LitePalCanvas litePalCanvas) {
        BmobCanvas bmobCanvas = new BmobCanvas();
        bmobCanvas.setCanvasName(litePalCanvas.getCanvasName());
        bmobCanvas.setCreator(litePalCanvas.getCreator());
        bmobCanvas.setCreatorID(litePalCanvas.getCreatorID());
        bmobCanvas.setPixelCount(litePalCanvas.getPixelCount());
        bmobCanvas.setJsonData(litePalCanvas.getJsonData());

        bmobCanvas.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    Toast.makeText(GalleryActivity.getInstance(), "作品发布成功！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GalleryActivity.getInstance(), "作品发布失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
