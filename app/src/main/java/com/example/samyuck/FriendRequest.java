package com.example.samyuck;

public class FriendRequest {
    private String fromUserId;
    private String toUserId;
    private String status;

    public FriendRequest() {}  // Firebase용 기본 생성자

    public FriendRequest(String fromUserId, String toUserId, String status) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.status = status;
    }

    public String getFromUserId() { return fromUserId; }
    public String getToUserId() { return toUserId; }
    public String getStatus() { return status; }

    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }
    public void setToUserId(String toUserId) { this.toUserId = toUserId; }
    public void setStatus(String status) { this.status = status; }
}
