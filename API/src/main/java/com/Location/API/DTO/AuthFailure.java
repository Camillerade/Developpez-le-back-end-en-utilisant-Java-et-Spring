package com.Location.API.DTO;

public class AuthFailure {
    private String message;

    public AuthFailure(String message) {
        this.message = message;
    }

    // Getter et Setter
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
