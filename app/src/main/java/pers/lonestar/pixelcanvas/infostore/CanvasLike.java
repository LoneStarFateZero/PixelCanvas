package pers.lonestar.pixelcanvas.infostore;

import cn.bmob.v3.BmobObject;

public class CanvasLike extends BmobObject {
    private BmobCanvas canvas;
    private PixelUser likeUser;

    public BmobCanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(BmobCanvas canvas) {
        this.canvas = canvas;
    }

    public PixelUser getLikeUser() {
        return likeUser;
    }

    public void setLikeUser(PixelUser likeUser) {
        this.likeUser = likeUser;
    }
}
