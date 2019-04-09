package pers.lonestar.pixelcanvas.utils;

import android.content.Context;
import android.content.SharedPreferences;

import cn.bmob.v3.update.BmobUpdateAgent;

public class UpdateUtils {
    public static void checkUpdate(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean autoUpdate = sharedPreferences.getBoolean("auto_update", true);
        //自动更新检测
        if (autoUpdate) {
            BmobUpdateAgent.forceUpdate(context);
        }
    }
}
