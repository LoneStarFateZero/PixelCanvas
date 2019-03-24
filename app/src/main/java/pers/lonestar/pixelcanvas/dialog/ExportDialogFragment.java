package pers.lonestar.pixelcanvas.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class ExportDialogFragment extends DialogFragment {
    private Button export;
    private TextView formatText;
    private AlertDialog dialog;
    private int format;
    private LitePalCanvas litePalCanvas;
    private View canvas;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        final View view = LayoutInflater.from(requireActivity()).inflate(R.layout.export_dialog, null);

        export = view.findViewById(R.id.export_button);
        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //根据选择的格式导出画布
                if (format == 0) {
                    //未选择导出格式，重新选择
                    Toast.makeText(requireContext(), "请选择导出格式", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.cancel();
                    //TODO
                    //导出
                    saveToSystemGallery(requireContext(), litePalCanvas, canvas, format);
                }
            }
        });

        formatText = view.findViewById(R.id.format_text);
        formatText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPopupButtonClick(v);
            }
        });

        builder.setView(view);
        dialog = builder.create();
        return dialog;
    }

    //弹出菜单
    private void onPopupButtonClick(final View view) {
        final PopupMenu popupMenu = new PopupMenu(requireActivity(), formatText);
        popupMenu.getMenuInflater().inflate(R.menu.format_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.format_svg:
                        format = 1;
                        break;
                    case R.id.format_png:
                        format = 2;
                        break;
                    case R.id.format_jpeg:
                        format = 3;
                        break;
                    case R.id.format_webp:
                        format = 4;
                        break;
                }
                formatText.setText(item.getTitle().toString());
                popupMenu.dismiss();
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.format_menu, menu);
    }

    private void saveToSystemGallery(Context context, LitePalCanvas litePalCanvas, View canvas, int format) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "PixelCanvas");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = null;
        Bitmap.CompressFormat exportFormat = null;
        switch (format) {
            case 1:
                fileName = litePalCanvas.getCanvasName() + ".svg";
                break;
            case 2:
                fileName = litePalCanvas.getCanvasName() + ".png";
                exportFormat = Bitmap.CompressFormat.PNG;
                break;
            case 3:
                fileName = litePalCanvas.getCanvasName() + ".jpg";
                exportFormat = Bitmap.CompressFormat.JPEG;
                break;
            case 4:
                fileName = litePalCanvas.getCanvasName() + ".webp";
                exportFormat = Bitmap.CompressFormat.WEBP;
                break;
        }
        Bitmap bmp = loadBitmapFromView(canvas);
        File file = new File(appDir, fileName);
        try {
            //导出其他图片格式
            if (exportFormat != null) {
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(exportFormat, 100, fos);
                fos.flush();
                fos.close();
            } else {
                //导出SVG文本
                exportSVG(file);
            }
            Toast.makeText(context, "图片已导出", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "图片导出失败，请检查设置", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "图片导出失败，请检查设置", Toast.LENGTH_SHORT).show();
        }

        /*
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
        */
    }

    //获取View的Bitmap，用于图像生成和设置
    private Bitmap loadBitmapFromView(View view) {
        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        if (format == 3)
            //避免JPEG格式不支持透明背景，产生黑色背景
            //故采用白色背景覆盖
            c.drawColor(Color.WHITE);
        else
            //其他格式支持采用透明背景
            c.drawColor(Color.TRANSPARENT);
        view.draw(c);
        return bmp;
    }

    //导出SVG格式
    private void exportSVG(File file) throws FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<svg version=\"1.1\" width=\"12\" height=\"12\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        for (int i = 0; i < ParameterUtils.pixelColor.length; i++) {
            for (int j = 0; j < ParameterUtils.pixelColor.length; j++) {
                if (ParameterUtils.pixelColor[i][j] != 0)
                    stringBuilder.append("<rect x=\"" + j + "\" y=\"" + i + "\" width=\"1\" height=\"1\" fill=\"#" + Integer.toHexString((ParameterUtils.pixelColor[i][j] & 0xff0000) >> 16) + Integer.toHexString((ParameterUtils.pixelColor[i][j] & 0x00ff00) >> 8) + Integer.toHexString(ParameterUtils.pixelColor[i][j] & 0x0000ff) + "\" />\n");
            }
        }
        stringBuilder.append("</svg>");

        PrintWriter printWriter = new PrintWriter(file);
        printWriter.write(stringBuilder.toString());
        printWriter.close();
    }

    public void initParameter(LitePalCanvas litePalCanvas, View canvas) {
        format = 0;
        this.litePalCanvas = litePalCanvas;
        this.canvas = canvas;
    }
}