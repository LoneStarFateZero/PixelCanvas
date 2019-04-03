package pers.lonestar.pixelcanvas.infostore;

import java.io.Serializable;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobRelation;

public class PixelUser extends BmobUser implements Serializable {
    private String nickname;
    private String avatarUrl;
    private String introduction;
    private BmobRelation followUsers;

    public BmobRelation getFollowUsers() {
        return followUsers;
    }

    public void setFollowUsers(BmobRelation followUsers) {
        this.followUsers = followUsers;
    }

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
