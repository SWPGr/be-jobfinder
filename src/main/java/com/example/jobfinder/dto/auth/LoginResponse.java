package com.example.jobfinder.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String token;
    private String role;


    public LoginResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }

}
