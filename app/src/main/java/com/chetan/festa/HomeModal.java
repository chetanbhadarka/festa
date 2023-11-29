package com.chetan.festa;

public class HomeModal {
    String email, postImage, postUid, userUid, username;
    String profileImage = "null";
    int likesCount;
    long timeStamp;

    HomeModal() {

    }

    public HomeModal(String email, String postImage, String postUid, String profileImage, String userUid, String username, int likesCount, long timeStamp) {
        this.email = email;
        this.postImage = postImage;
        this.postUid = postUid;
        this.profileImage = profileImage;
        this.userUid = userUid;
        this.username = username;
        this.likesCount = likesCount;
        this.timeStamp = timeStamp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostUid() {
        return postUid;
    }

    public void setPostUid(String postUid) {
        this.postUid = postUid;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
