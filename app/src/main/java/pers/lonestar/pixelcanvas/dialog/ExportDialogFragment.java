package pers.lonestar.pixelcanvas.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import pers.lonestar.pixelcanvas.PixelApp;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.infostore.FileCanvas;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;
import pers.lonestar.pixelcanvas.utils.ParameterUtils;

public class ExportDialogFragment extends DialogFragment {
    private static final int FORMAT_SVG = 1;
    private static final int FORMAT_PIXEL = 5;
    private static final int FORMAT_PNG = 2;
    private static final int FORMAT_JPEG = 3;
    private static final int FORMAT_WEBP = 4;
    private Button export;
    private TextView formatText;
    private LinearLayout pixelScale;
    private EditText pixelScaleEditText;
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

        pixelScale = view.findViewById(R.id.pixel_scale);
        pixelScaleEditText = view.findViewById(R.id.pixel_scale_edittext);

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
                        format = FORMAT_SVG;
                        pixelScale.setVisibility(View.VISIBLE);
                        pixelScaleEditText.setText("1");
                        break;
                    case R.id.format_pixel:
                        format = FORMAT_PIXEL;
                        pixelScale.setVisibility(View.GONE);
                        break;
                    case R.id.format_png:
                        format = FORMAT_PNG;
                        pixelScale.setVisibility(View.GONE);
                        break;
                    case R.id.format_jpeg:
                        format = FORMAT_JPEG;
                        pixelScale.setVisibility(View.GONE);
                        break;
                    case R.id.format_webp:
                        format = FORMAT_WEBP;
                        pixelScale.setVisibility(View.GONE);
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
            case FORMAT_SVG:
                if (pixelScaleEditText.getText().toString().equals("")) {
                    Toast.makeText(context, "像素尺寸不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                fileName = litePalCanvas.getCanvasName() + ".svg";
                break;
            case FORMAT_PIXEL:
                fileName = litePalCanvas.getCanvasName() + ".canvas";
                break;
            case FORMAT_PNG:
                fileName = litePalCanvas.getCanvasName() + ".png";
                exportFormat = Bitmap.CompressFormat.PNG;
                break;
            case FORMAT_JPEG:
                fileName = litePalCanvas.getCanvasName() + ".jpg";
                exportFormat = Bitmap.CompressFormat.JPEG;
                break;
            case FORMAT_WEBP:
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
                //通知系统图库扫描更新
                MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, new String[]{"image/png", "image/jpeg", "image/webp"}, null);
                fos.flush();
                fos.close();
            } else {
                if (format == FORMAT_SVG)
                    //导出SVG文本
                    exportSVG(file);
                else if (format == FORMAT_PIXEL)
                    exportPixelCanvas(file);
            }
            Toast.makeText(context, "图片已导出", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "图片导出失败，请检查设置", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "图片导出失败，请检查设置", Toast.LENGTH_SHORT).show();
        }
    }

    //获取View的Bitmap，用于图像生成和设置
    private Bitmap loadBitmapFromView(View view) {
        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        if (format == FORMAT_JPEG)
            //避免JPEG格式不支持透明背景，产生黑色背景
            //故采用白色背景覆盖
            c.drawColor(Color.WHITE);
        else
            //其他格式支持采用透明背景
            c.drawColor(Color.TRANSPARENT);
        view.draw(c);
        return bmp;
    }

    //导出SVG格式，默认像素大小为1
    private void exportSVG(File file) throws FileNotFoundException {
        int pixelScaleSize = Integer.parseInt(pixelScaleEditText.getEditableText().toString());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<svg version=\"1.1\" width=\"" + PixelApp.pixelColor.length * pixelScaleSize + "\" height=\"" + PixelApp.pixelColor.length * pixelScaleSize + "\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        for (int i = 0; i < PixelApp.pixelColor.length; i++) {
            for (int j = 0; j < PixelApp.pixelColor.length; j++) {
                if (PixelApp.pixelColor[i][j] != 0)
                    stringBuilder.append("<rect x=\"")
                            .append(j * pixelScaleSize)
                            .append("\" y=\"")
                            .append(i * pixelScaleSize)
                            .append("\" width=\"")
                            .append(pixelScaleSize)
                            .append("\" height=\"")
                            .append(pixelScaleSize)
                            .append("\" fill=\"")
                            .append(ParameterUtils.intColortoHexColor(PixelApp.pixelColor[i][j]))
                            .append("\" />\n");
            }
        }
        stringBuilder.append("</svg>");

        PrintWriter printWriter = new PrintWriter(file);
        printWriter.write(stringBuilder.toString());
        printWriter.close();
    }

    //导出成pixel canvas自定义类型文件
    private void exportPixelCanvas(File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        FileCanvas fileCanvas = new FileCanvas();
        fileCanvas.setCanvasName(litePalCanvas.getCanvasName());
        fileCanvas.setCreatorID(litePalCanvas.getCreatorID());
        fileCanvas.setPixelCount(litePalCanvas.getPixelCount());
        fileCanvas.setJsonData(litePalCanvas.getJsonData());
        fileCanvas.setCreatedAt(litePalCanvas.getCreatedAt());
        fileCanvas.setUpdatedAt(litePalCanvas.getUpdatedAt());
        fileCanvas.setThumbnail(litePalCanvas.getThumbnail());
        objectOutputStream.writeObject(fileCanvas);
        objectOutputStream.close();
    }

    public void initParameter(LitePalCanvas litePalCanvas, View canvas) {
        format = 0;
        this.litePalCanvas = litePalCanvas;
        this.canvas = canvas;
    }
}
