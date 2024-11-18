package com.Location.API.DTO;

public class AuthSuccess {
    private String token;

    public AuthSuccess(String token) {
        this.token = token;
    }

    // Getter et Setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
