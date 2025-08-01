package com.example.jobfinder.dto.user;

import com.example.jobfinder.dto.simple.SimpleNameResponse;
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

    private SimpleNameResponse role;
    private String fullName;
    private String phone;
    private String location;
    private Integer verified;
    private String avatarUrl;
    private Boolean active;

    // Các trường Employer
    private String companyName;
    private String website;
    private String banner;
    private String teamSize;
    private Integer yearOfEstablishment;
    private String mapLocation;
    private SimpleNameResponse organization;

    // Các trường JobSeeker
    private String resumeUrl; // <-- Đảm bảo trường này có trong UserResponse
    private SimpleNameResponse education; // <-- Dùng SimpleNameResponse cho Education
    private SimpleNameResponse experience; // <-- Dùng SimpleNameResponse cho Experience

    // Các trường tính toán
    private Long totalJobsPosted;
    private Long totalApplications;

    // Additional fields for employer metrics
    private String description;
    private Float averageRating;
    private Integer totalReviews;
}