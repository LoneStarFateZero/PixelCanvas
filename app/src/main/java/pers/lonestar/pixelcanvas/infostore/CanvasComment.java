package pers.lonestar.pixelcanvas.infostore;

import cn.bmob.v3.BmobObject;

public class CanvasComment extends BmobObject {
    private BmobCanvas canvas;
    private PixelUser commentUser;
    private String commentText;

    public BmobCanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(BmobCanvas canvas) {
        this.canvas = canvas;
    }

    public PixelUser getCommentUser() {
        return commentUser;
    }

    public void setCommentUser(PixelUser commentUser) {
        this.commentUser = commentUser;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}
