package com.example.jobfinder.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RegisterRequest {
    private String email;
    private String password;

    private String roleName;
}
