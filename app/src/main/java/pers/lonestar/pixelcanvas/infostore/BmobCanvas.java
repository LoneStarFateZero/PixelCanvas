package pers.lonestar.pixelcanvas.infostore;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class BmobCanvas extends BmobObject {
    //作品名称
    private String canvasName;
    //创作者唯一ID
    private PixelUser creator;
    //喜欢此作品的用户
    //一个作品可被很多用户喜欢
    //一个用户也可喜欢很多作品
    //多对多关系
    private BmobRelation likes;
    //尺寸
    private int pixelCount;
    //json像素数据
    private String jsonData;
    //字节流缩略图
    private byte[] thumbnail;

    public BmobRelation getLikes() {
        return likes;
    }

    public void setLikes(BmobRelation likes) {
        this.likes = likes;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public PixelUser getCreator() {
        return creator;
    }

    public void setCreator(PixelUser creator) {
        this.creator = creator;
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
}
