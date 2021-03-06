package pers.lonestar.pixelcanvas;

import org.litepal.LitePalApplication;

import cn.bmob.v3.Bmob;
import pers.lonestar.pixelcanvas.infostore.LitePalCanvas;
import pers.lonestar.pixelcanvas.utils.FontsUtils;

public class PixelApp extends LitePalApplication {
    private static PixelApp instance;
    //默认头像URL
    public static final String defaultAvatarUrl = "https://bmob-cdn-23980.b0.upaiyun.com/2019/03/29/6bf99f6e40efcf3480e0c62ab7558adb.gif";
    public static int[][] pixelColor;
    //    public static PixelUser pixelUser;
    public static LitePalCanvas litePalCanvas;

    public static PixelApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //替换全局字体
        FontsUtils.setDefaultFont(this, "MONOSPACE", "fonts/zpix.ttf");
        //Bmob服务初始化
        Bmob.initialize(this, "77f66f44e9afcf7c2eccd97c0885354b");
    }
}
