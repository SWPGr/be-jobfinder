package com.example.jobfinder.dto.auth;


import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.model.Education;
import com.example.jobfinder.model.Experience;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProfileResponse {
    private Long id; // ID của User
    private String email;
    private String roleName;
    private Boolean isPremium; // Nếu bạn muốn hiển thị trạng thái Premium
    private Integer verified; // Trạng thái xác minh
    private LocalDateTime createdAt; // Thời gian tạo
    private LocalDateTime updatedAt; // Thời gian cập nhật

    // Các trường chung từ UserDetail
    private String fullName;
    private String phone;
    private String location;
    private String avatarUrl;


    // Các trường đặc thù cho Job Seeker
    private String resumeUrl;
    private Long educationId;
    private String educationName;
    private Long experienceId;
    private String experienceName;
    // Các trường đặc thù cho Employer
    private String companyName;
    private String website;
    private String avatarUrl;
    private Long organizationId;
    private String organizationType;
    private String banner;
    private String teamSize;
    private Integer yearOfEstablishment;
    private String mapLocation;
    private String description;

    // Các trường tính toán (cần được gán thủ công trong service)
    private Long totalJobsPosted;
    private Long totalApplications;
}
