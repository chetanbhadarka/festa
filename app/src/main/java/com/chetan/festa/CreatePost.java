package com.chetan.festa;

import java.util.Map;

public class CreatePost {
    public String email, username, userUid, profileImage, postImage, postUid;
    public int likesCount;
    public Map timeStamp;

    public CreatePost() {

    }

    public CreatePost(String email, String username, String userUid, String profileImage, String postImage, int likesCount, Map timeStamp, String postUid) {
        this.email = email;
        this.username = username;
        this.userUid = userUid;
        this.profileImage = profileImage;
        this.postImage = postImage;
        this.likesCount = likesCount;
        this.timeStamp = timeStamp;
        this.postUid = postUid;
    }
}
