package com.example.jobfinder.dto.application;

import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.model.enums.ApplicationStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder // Đảm bảo bạn có constructor hoặc @Builder để dễ tạo
public class ApplicationResponse {
    private Long id;
    private UserResponse jobSeeker;
    private JobResponse job;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}