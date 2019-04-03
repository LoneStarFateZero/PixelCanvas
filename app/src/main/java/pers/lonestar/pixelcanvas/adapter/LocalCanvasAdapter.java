package pers.lonestar.pixelcanvas.adapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.GalleryActivity;
import pers.lonestar.pixelcanvas.activity.PaintActivity;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;
import pers.lonestar.pixelcanvas.infostore.PixelUser;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class LocalCanvasAdapter extends RecyclerView.Adapter<LocalCanvasAdapter.ViewHolder> {
    private List<LitePalCanvas> litePalCanvasList;

    public LocalCanvasAdapter(List<LitePalCanvas> litePalCanvasList) {
        this.litePalCanvasList = litePalCanvasList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_canvas_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final LitePalCanvas litePalCanvas = litePalCanvasList.get(position);
        //图片加载或许需要优化
        //使用Glide加载图片会造成OOM
        //避免每次都要重新生成缩略图的Bitmap
        holder.thumbnailBitmap = ParameterUtils.bytesToBitmap(litePalCanvas.getThumbnail());
        Glide.with(GalleryActivity.getInstance()).load(holder.thumbnailBitmap).into(holder.thumbnail);
        holder.canvasName.setText(litePalCanvas.getCanvasName());
        holder.canvasSize.setText("尺寸:" + litePalCanvas.getPixelCount() + "x" + litePalCanvas.getPixelCount());
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
                                showPostDialog(litePalCanvas);
                                break;
                            case R.id.list_rename:
                                showRenameDialog(litePalCanvas, position);
                                break;
                            case R.id.list_copy:
                                showCopyDialog(litePalCanvas);
                                break;
                            case R.id.list_delete:
                                showDeleteDialog(litePalCanvas, position);
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
        Bitmap thumbnailBitmap;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            canvasItemView = itemView;
            thumbnail = itemView.findViewById(R.id.item_canvas_image);
            canvasName = itemView.findViewById(R.id.item_canvas_name);
            canvasSize = itemView.findViewById(R.id.item_canvas_size);
            canvasUpdated = itemView.findViewById(R.id.item_canvas_updated);
            canvasMenu = itemView.findViewById(R.id.item_menu);
            thumbnailBitmap = null;
        }
    }

    //发布对话框
    private void showPostDialog(final LitePalCanvas litePalCanvas) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GalleryActivity.getInstance());
        dialog.setMessage("确定要发布这个作品吗？");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postCanvasFile(litePalCanvas);
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    //重命名对话框
    private void showRenameDialog(final LitePalCanvas litePalCanvas, final int position) {
        View view = View.inflate(GalleryActivity.getInstance(), R.layout.rename_dialog, null);
        final EditText renameText = view.findViewById(R.id.rename_text);
        renameText.setText(litePalCanvas.getCanvasName());
        renameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameText.selectAll();
            }
        });
        AlertDialog.Builder dialog = new AlertDialog.Builder(GalleryActivity.getInstance());
        dialog.setView(view);
        dialog.setTitle("重命名");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = renameText.getText().toString();
                if (!newName.equals("")) {
                    litePalCanvas.setCanvasName(newName);
                    litePalCanvas.save();
                    notifyItemChanged(position);
                } else {
                    Toast.makeText(GalleryActivity.getInstance(), "名称不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    //复制对话框
    private void showCopyDialog(final LitePalCanvas litePalCanvas) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GalleryActivity.getInstance());
        dialog.setMessage("确定要复制这个作品吗？");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //复制作品
                //获取本地日期时间格式
                DateFormat dateFormat = DateFormat.getDateTimeInstance();
                //获取当前时间
                Date date = new Date(System.currentTimeMillis());
                LitePalCanvas copyCanvas = new LitePalCanvas();
                copyCanvas.setCanvasName(litePalCanvas.getCanvasName());
                copyCanvas.setCreatorID(litePalCanvas.getCreatorID());
                copyCanvas.setPixelCount(litePalCanvas.getPixelCount());
                copyCanvas.setJsonData(litePalCanvas.getJsonData());
                copyCanvas.setCreatedAt(litePalCanvas.getCreatedAt());
                copyCanvas.setUpdatedAt(dateFormat.format(date));
                copyCanvas.setThumbnail(litePalCanvas.getThumbnail());
                copyCanvas.save();
                litePalCanvasList.add(0, copyCanvas);
                notifyItemInserted(0);
                //调用此方法刷新数据，否则其后position不会自动+1，导致错误
                notifyItemRangeChanged(0, getItemCount());
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    //删除对话框
    private void showDeleteDialog(final LitePalCanvas litePalCanvas, final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GalleryActivity.getInstance());
        dialog.setMessage("确定要删除这个作品吗？\n操作无法恢复");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //删除画布
                litePalCanvasList.remove(position);
                notifyItemRemoved(position);
                //调用此方法刷新数据，否则其后position不会自动+1，导致错误
                notifyItemRangeChanged(position, getItemCount());
                litePalCanvas.delete();
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    //发布作品
    private void postCanvasFile(LitePalCanvas litePalCanvas) {
        BmobCanvas bmobCanvas = new BmobCanvas();
        bmobCanvas.setCanvasName(litePalCanvas.getCanvasName());
        bmobCanvas.setCreator(BmobUser.getCurrentUser(PixelUser.class));
        bmobCanvas.setPixelCount(litePalCanvas.getPixelCount());
        bmobCanvas.setJsonData(litePalCanvas.getJsonData());
        bmobCanvas.setThumbnail(litePalCanvas.getThumbnail());

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
