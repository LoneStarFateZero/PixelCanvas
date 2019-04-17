package pers.lonestar.pixelcanvas.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.GalleryActivity;
import pers.lonestar.pixelcanvas.activity.PaintActivity;
import pers.lonestar.pixelcanvas.activity.PublishActivity;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;
import pers.lonestar.pixelcanvas.infostore.ShowCheckBox;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class LocalCanvasAdapter extends RecyclerView.Adapter<LocalCanvasAdapter.ViewHolder> {
    private List<LitePalCanvas> litePalCanvasList;
    private List<Boolean> litePalCanvasChooseList;
    private ShowCheckBox showCheckBox;

    public LocalCanvasAdapter(List<LitePalCanvas> litePalCanvasList, List<Boolean> litePalCanvasChooseList, ShowCheckBox showCheckBox) {
        this.litePalCanvasList = litePalCanvasList;
        this.litePalCanvasChooseList = litePalCanvasChooseList;
        this.showCheckBox = showCheckBox;
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
        //显示选择框
        if (showCheckBox.isShowCheckBox()) {
            holder.canvasMenu.setVisibility(View.INVISIBLE);
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(litePalCanvasChooseList.get(position));
            holder.canvasMenu.setOnClickListener(null);
            holder.canvasItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    litePalCanvasChooseList.set(position, !holder.checkBox.isChecked());
                    holder.checkBox.setChecked(!holder.checkBox.isChecked());
                }
            });
            holder.canvasItemView.setOnLongClickListener(null);
        } else {
            holder.canvasMenu.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.GONE);
            holder.canvasMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(v, litePalCanvas, position);
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
            holder.canvasItemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPopupMenu(v, litePalCanvas, position);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return litePalCanvasList.size();
    }

    //弹出菜单
    private void showPopupMenu(View v, final LitePalCanvas litePalCanvas, final int position) {
        //弹出菜单
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.canvas_list_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.list_post:
                        Intent intent = new Intent(GalleryActivity.getInstance(), PublishActivity.class);
                        intent.putExtra("local_canvas", litePalCanvas);
                        GalleryActivity.getInstance().startActivity(intent);
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

    //重命名对话框
    private void showRenameDialog(final LitePalCanvas litePalCanvas, final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(GalleryActivity.getInstance());
        View view = View.inflate(GalleryActivity.getInstance(), R.layout.rename_dialog, null);
        final EditText renameText = view.findViewById(R.id.rename_text);
        renameText.setText(litePalCanvas.getCanvasName());
        renameText.selectAll();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                renameText.requestFocus();
                InputMethodManager inputManager =
                        (InputMethodManager) renameText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(renameText, 0);
            }
        }, 300);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        View canvasItemView;
        ImageView thumbnail;
        TextView canvasName;
        TextView canvasSize;
        TextView canvasUpdated;
        ImageView canvasMenu;
        Bitmap thumbnailBitmap;
        MaterialCheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            canvasItemView = itemView;
            thumbnail = itemView.findViewById(R.id.item_canvas_image);
            canvasName = itemView.findViewById(R.id.item_canvas_name);
            canvasSize = itemView.findViewById(R.id.item_canvas_size);
            canvasUpdated = itemView.findViewById(R.id.item_canvas_updated);
            canvasMenu = itemView.findViewById(R.id.item_menu);
            checkBox = itemView.findViewById(R.id.item_canvas_checkbox);
            thumbnailBitmap = null;
        }
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
}
