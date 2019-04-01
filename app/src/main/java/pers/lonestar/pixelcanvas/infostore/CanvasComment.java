package pers.lonestar.pixelcanvas.infostore;

import cn.bmob.v3.BmobObject;

public class CanvasComment extends BmobObject {
    private String canvasId;
    private String commentUserId;
    private String commentText;

    public String getCanvasId() {
        return canvasId;
    }

    public void setCanvasId(String canvasId) {
        this.canvasId = canvasId;
    }

    public String getCommentUserId() {
        return commentUserId;
    }

    public void setCommentUserId(String commentUserId) {
        this.commentUserId = commentUserId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

}
