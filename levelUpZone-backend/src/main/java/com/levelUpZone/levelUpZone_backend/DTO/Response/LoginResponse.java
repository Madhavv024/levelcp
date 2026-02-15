package com.levelUpZone.levelUpZone_backend.DTO.Response;

public class LoginResponse {

    private String token;
    private String userEmail;
    private Integer userId;
    private String cfId;

    public LoginResponse() {
    }

    public LoginResponse(String token) {
        this.token = token;
    }

    public LoginResponse(String token, String userEmail, Integer userId,  String cfId) {
        this.token = token;
        this.userEmail = userEmail;
        this.userId = userId;
        this.cfId = cfId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getCfId() {
        return cfId;
    }
    public void setCfId(String cfId) {
        this.cfId = cfId;
    }
}
