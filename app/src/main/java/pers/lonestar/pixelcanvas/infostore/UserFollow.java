package pers.lonestar.pixelcanvas.infostore;

import cn.bmob.v3.BmobObject;

public class UserFollow extends BmobObject {
    private String userId;
    private String followUserId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFollowUserId() {
        return followUserId;
    }

    public void setFollowUserId(String followUserId) {
        this.followUserId = followUserId;
    }
}
