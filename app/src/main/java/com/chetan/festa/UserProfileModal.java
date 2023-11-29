package com.chetan.festa;

public class UserProfileModal {
    String email, postImage, postUid, userUid, username;
    String profileImage = "null";
    int likesCount;
    long timeStamp;

    UserProfileModal(){

    }

    public UserProfileModal(String email, String postImage, String postUid, String userUid, String username, String profileImage, int likesCount, long timeStamp) {
        this.email = email;
        this.postImage = postImage;
        this.postUid = postUid;
        this.userUid = userUid;
        this.username = username;
        this.profileImage = profileImage;
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
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
