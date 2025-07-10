
package com.example.jobfinder.dto.user;

import com.example.jobfinder.dto.simple.SimpleNameResponse; // <-- Import RoleResponse
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    private Long id;
    private String email;
    private Boolean isPremium;
    private LocalDateTime createdAt; // Dùng LocalDateTime
    private LocalDateTime updatedAt; // Dùng LocalDateTime

    private String roleName;
    private String fullName;
    private String phone;
    private String location;
    private Integer verified;
    private String avatarUrl;

    // Các trường Employer
    private String companyName;
    private String website;
    private String banner;
    private String teamSize;
    private Integer yearOfEstablishment;
    private String mapLocation;
    private String organizationType;

    // Các trường JobSeeker
    private String resumeUrl; // <-- Đảm bảo trường này có trong UserResponse
    private SimpleNameResponse education; // <-- Dùng SimpleNameResponse cho Education
    private SimpleNameResponse experience; // <-- Dùng SimpleNameResponse cho Experience

    // Các trường tính toán
    private Long totalJobsPosted;
    private Long totalApplications;
}