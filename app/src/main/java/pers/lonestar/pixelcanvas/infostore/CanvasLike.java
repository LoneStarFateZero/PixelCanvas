package pers.lonestar.pixelcanvas.infostore;

import cn.bmob.v3.BmobObject;
import pers.lonestar.pixelcanvas.customview.PixelCanvas;

public class CanvasLike extends BmobObject {
    private PixelCanvas canvas;
    private PixelUser likeUser;

    public PixelCanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(PixelCanvas canvas) {
        this.canvas = canvas;
    }

    public PixelUser getLikeUser() {
        return likeUser;
    }

    public void setLikeUser(PixelUser likeUser) {
        this.likeUser = likeUser;
    }
}
