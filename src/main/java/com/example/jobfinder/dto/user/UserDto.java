package com.example.jobfinder.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String fullName; // Phải có nếu bạn muốn map từ User.fullName
    private Boolean isPremium;
    private Integer verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive; // <-- Đảm bảo trường này tồn tại và đúng kiểu
    private String roleName;
}