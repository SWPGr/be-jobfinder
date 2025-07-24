package com.example.jobfinder.dto.auth;

import com.example.jobfinder.dto.user.UserDto;
import com.example.jobfinder.dto.user.UserResponse;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String roleName;
    private UserDto user;
    private Integer code;
    private String message;

}
