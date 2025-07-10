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
    private ApplicantResponse jobSeeker;
    private JobResponse job;
<<<<<<< HEAD
    private String status;
    private String email;
    private String phone;
    private String resume;
    private String coverLetter;
=======
    private ApplicationStatus status;
>>>>>>> 9125635c534fa49d4e82a6d4b822f01e31aa7529
    private LocalDateTime appliedAt;
}