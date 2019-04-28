package pers.lonestar.pixelcanvas.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

public class ParameterUtils {
    public static int canvasWidth = 960;

    public static int[] initColorCard = new int[]{0xFF000000, 0xFF222034, 0xFF45283c, 0xFF663931,
            0xFF8f563b, 0xFFdf7126, 0xFFd9a066, 0xFFeec39a,
            0xFFfbf236, 0xFF99e550, 0xFF6abe30, 0xFF37946e,
            0xFF4b692f, 0xFF524b24, 0xFF323c39, 0xFF3f3f74,
            0xFF306082, 0xFF5b6ee1, 0xFF639bff, 0xFF5fcde4,
            0xFFcbdbfc, 0xFFffffff, 0xFF9badb7, 0xFF847e87,
            0xFF696a6a, 0xFF595652, 0xFF76428a, 0xFFac3232,
            0xFFd95763, 0xFFd77bba, 0xFF8f974a, 0xFF8a6f30};

    public static String intColortoHexColor(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

    public static String intColortoHexRed(int color) {
        String red = Integer.toHexString(Color.red(color));

        if (red.length() == 1) {
            red = "0" + red;
        }
        return red;
    }

    public static String intColortoHexGreen(int color) {
        String green = Integer.toHexString(Color.green(color));

        if (green.length() == 1) {
            green = "0" + green;
        }
        return green;
    }

    public static String intColortoHexBlue(int color) {
        String blue = Integer.toHexString(Color.blue(color));

        if (blue.length() == 1) {
            blue = "0" + blue;
        }
        return blue;
    }

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static Bitmap bytesToBitmap(byte[] imgByte) {
//        return BitmapFactory.decodeByteArray(img, 0, img.length);
        //优化Bitmap生成，否则极易产生OOM
        InputStream input = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        input = new ByteArrayInputStream(imgByte);
        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(input, null, options));
        bitmap = (Bitmap) softRef.get();
        imgByte = null;
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
