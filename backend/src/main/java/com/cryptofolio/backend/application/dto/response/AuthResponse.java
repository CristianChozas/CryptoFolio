package com.cryptofolio.backend.application.dto.response;

public class AuthResponse {

    private final String token;
    private final Long userId;
    private final String username;

    public AuthResponse(String token, Long userId, String username) {
        this.token = token;
        this.userId = userId;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
