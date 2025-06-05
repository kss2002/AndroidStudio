package com.example.samyuck;

public class NotificationItem {
    private String fromUserId;
    private String message;
    private long timestamp;
    private boolean isRead;

    // Firebase를 위한 기본 생성자
    public NotificationItem() {}

    public NotificationItem(String fromUserId, String message, long timestamp, boolean isRead) {
        this.fromUserId = fromUserId;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getFromUserId() {
        return fromUserId != null ? fromUserId : "";
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
