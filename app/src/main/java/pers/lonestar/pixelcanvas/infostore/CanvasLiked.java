package pers.lonestar.pixelcanvas.infostore;

import cn.bmob.v3.BmobObject;

public class CanvasLiked extends BmobObject {
    private String canvasId;
    private String likedUserId;

    public String getCanvasId() {
        return canvasId;
    }

    public void setCanvasId(String canvasId) {
        this.canvasId = canvasId;
    }

    public String getLikedUserId() {
        return likedUserId;
    }

    public void setLikedUserId(String likedUserId) {
        this.likedUserId = likedUserId;
    }
}
