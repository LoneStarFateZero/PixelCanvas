package pers.lonestar.pixelcanvas.listener;

import pers.lonestar.pixelcanvas.infostore.CanvasComment;

public abstract class CommentInsertListener {
    //通过此方法将对话框中发表的评论插入到评论列表中，刷新数据
    public void insertComment(CanvasComment canvasComment) {
    }
}
