package com.example.jobfinder.dto;

import lombok.Getter;
import lombok.Setter;


public class LoginResponse {
    private String token;
    private String role;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LoginResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }

}
