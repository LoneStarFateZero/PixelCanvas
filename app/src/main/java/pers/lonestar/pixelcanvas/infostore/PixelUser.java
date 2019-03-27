package pers.lonestar.pixelcanvas.infostore;

import java.io.Serializable;

import cn.bmob.v3.BmobUser;

public class PixelUser extends BmobUser implements Serializable {
    private String nickname;
    private String avatarUrl;
    private String introduction;

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
