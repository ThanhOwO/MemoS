package com.Thanh.memos.model;

public class ChatUserModel {
    private String id, userUid, name, imageURL;

    public ChatUserModel() {
    }

    public ChatUserModel(String id, String userUid, String name, String imageURL) {
        this.id = id;
        this.userUid = userUid;
        this.name = name;
        this.imageURL = imageURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
