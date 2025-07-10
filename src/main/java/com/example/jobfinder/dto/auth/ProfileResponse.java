package com.example.jobfinder.dto.auth;

<<<<<<< HEAD
=======
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.model.Education;
import com.example.jobfinder.model.Experience;
import lombok.Data;
>>>>>>> 9125635c534fa49d4e82a6d4b822f01e31aa7529
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
<<<<<<< HEAD
    private Long organizationId;
    private String organizationType;
=======

    // Các trường đặc thù cho Job Seeker
    private String resumeUrl;
    private Education education;
    private Experience experience;
    // Các trường đặc thù cho Employer
    private String companyName;
    private String website;
    private String banner;
    private String teamSize;
    private Integer yearOfEstablishment;
    private String mapLocation;
    private String organizationType;
    private String description;

    // Các trường tính toán (cần được gán thủ công trong service)
    private Long totalJobsPosted;
    private Long totalApplications;
>>>>>>> 9125635c534fa49d4e82a6d4b822f01e31aa7529
}
