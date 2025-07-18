// src/main/java/com/example/jobfinder/dto/job_seeker/JobSeekerResponse.java
package com.example.jobfinder.dto.user;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerResponse {
    private Long userId; // ID của UserDetail
    private String userEmail; // Email của User (từ User)
    private String fullName;  // Từ UserDetail
    private String phone;     // Từ UserDetail
    private String location;  // Từ UserDetail

    private Long experienceId;
    private String experienceName;
    
    private String resumeUrl;

    // Có thể thêm Education nếu muốn hiển thị
    private Long educationId; // ID của Education
    private String educationName; // Tên của Education (nếu có)

    private Long totalApplications;
}