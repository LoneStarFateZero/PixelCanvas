package pers.lonestar.pixelcanvas.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class ParameterUtils {
    public static int canvasWidth = 960;

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static Bitmap bytesToBitmap(byte[] img) {
        return BitmapFactory.decodeByteArray(img, 0, img.length);
    }
}
