package pers.lonestar.pixelcanvas.infostore;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class LitePalCanvas extends LitePalSupport implements Serializable {
    //作品名称
    private String canvasName;
    //创作者唯一ID
    private String creatorID;
    //尺寸
    private int pixelCount;
    //json像素数据
    private String jsonData;
    //创作时间
    private String createdAt;
    //更新时间
    private String updatedAt;
    //字节流缩略图
    private byte[] thumbnail;

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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }
}
