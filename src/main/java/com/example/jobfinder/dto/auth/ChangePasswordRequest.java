package com.example.jobfinder.dto.auth;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}
