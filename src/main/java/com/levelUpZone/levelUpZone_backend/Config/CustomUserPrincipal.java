package com.levelUpZone.levelUpZone_backend.Config;


public class CustomUserPrincipal {

    private final Long userId;
    private final String username;

    public CustomUserPrincipal(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
