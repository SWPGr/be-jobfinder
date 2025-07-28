// src/main/java/com/example/jobfinder/dto/CandidateDetailResponse.java
package com.example.jobfinder.dto.job;

import com.example.jobfinder.dto.user.JobSeekerResponse;
import com.example.jobfinder.model.UserDetail;
import com.example.jobfinder.model.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDetailResponse {
    private Long userId;
    private Long applicationId;
    private String fullname;
    private String email;
    private String role;
    private ApplicationStatus status;
    private JobSeekerResponse seekerDetail; // Thông tin chi tiết của ứng viên
}