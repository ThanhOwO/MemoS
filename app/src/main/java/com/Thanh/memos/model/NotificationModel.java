package com.Thanh.memos.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationModel {
    String id, notification, oppositeID;
    @ServerTimestamp
    Date time;

    public NotificationModel() {
    }

    public NotificationModel(String id, String notification, String oppositeID, Date time) {
        this.id = id;
        this.notification = notification;
        this.oppositeID = oppositeID;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getOppositeID() {
        return oppositeID;
    }

    public void setOppositeID(String oppositeID) {
        this.oppositeID = oppositeID;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
