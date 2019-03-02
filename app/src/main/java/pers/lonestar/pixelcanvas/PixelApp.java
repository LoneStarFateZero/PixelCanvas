package pers.lonestar.pixelcanvas;

import android.app.Application;

import pers.lonestar.pixelcanvas.utils.FontsUtils;

public class PixelApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FontsUtils.setDefaultFont(this, "MONOSPACE", "fonts/zpix.ttf");
    }
}
