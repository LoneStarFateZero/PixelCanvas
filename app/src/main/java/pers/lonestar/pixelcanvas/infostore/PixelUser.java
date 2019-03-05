package pers.lonestar.pixelcanvas.infostore;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;

public class PixelUser extends BmobUser {
    private String nickname;
    private BmobFile avatar;
    private String introduction;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public BmobFile getAvatar() {
        return avatar;
    }

    public void setAvatar(BmobFile avatar) {
        this.avatar = avatar;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
