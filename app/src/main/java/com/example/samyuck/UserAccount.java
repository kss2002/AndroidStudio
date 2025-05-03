package com.example.samyuck;

public class UserAccount {

    private String idToken;     // Firebase UID
    private String emailId;     // 이메일
    private String password;    // 비밀번호
    private String name;        // 사용자 이름

    public UserAccount() {
        // Firebase에서 데이터를 가져올 때 사용되는 빈 생성자
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}