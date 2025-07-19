package com.example.jobfinder.dto.application;

import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.user.JobSeekerResponse;
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
    private LocalDateTime appliedAt;
    private String status;
    private String resume; // Tên trường trong ApplicationMapper là 'resume', nên để khớp ở đây là 'resume' (hoặc 'resumeUrl' nếu bạn thay đổi mapper)
    private String coverLetter;
    private String email; // Giữ lại nếu Application có trường này
    private String phone; // Giữ lại nếu Application có trường này
    private String applicationNote; // Giữ lại nếu có

    private JobSeekerResponse jobSeeker;

    // Thông tin chi tiết về công việc đã ứng tuyển
    private JobResponse job;
}