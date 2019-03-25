package pers.lonestar.pixelcanvas.infostore;

import cn.bmob.v3.BmobObject;

public class BmobCanvas extends BmobObject {
    //作品名称
    private String canvasName;
    //创作者名称
    private String creator;
    //创作者唯一ID
    private String creatorID;
    //尺寸
    private int pixelCount;
    //json像素数据
    private String jsonData;
    //字节流缩略图
    private byte[] thumbnail;

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public String getCanvasName() {
        return canvasName;
    }

    public void setCanvasName(String canvasName) {
        this.canvasName = canvasName;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getPixelCount() {
        return pixelCount;
    }

    public void setPixelCount(int pixelCount) {
        this.pixelCount = pixelCount;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}
