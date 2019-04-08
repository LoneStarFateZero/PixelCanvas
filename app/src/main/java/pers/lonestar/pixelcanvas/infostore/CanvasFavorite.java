package pers.lonestar.pixelcanvas.infostore;

import cn.bmob.v3.BmobObject;

public class CanvasFavorite extends BmobObject {
    private BmobCanvas canvas;
    private PixelUser creator;
    private PixelUser favoriteUser;

    public BmobCanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(BmobCanvas canvas) {
        this.canvas = canvas;
    }

    public PixelUser getCreator() {
        return creator;
    }

    public void setCreator(PixelUser creator) {
        this.creator = creator;
    }

    public PixelUser getFavoriteUser() {
        return favoriteUser;
    }

    public void setFavoriteUser(PixelUser favoriteUser) {
        this.favoriteUser = favoriteUser;
    }
}
