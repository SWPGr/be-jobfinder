package com.example.jobfinder.dto.job;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobUpdateRequest {
    private Long id;
    private String title;
    private String description;
    private String location;
    private Double salaryMin;
    private Double salaryMax;
    private String createdAt; // Hoặc LocalDateTime nếu bạn giữ nguyên kiểu
    private String updatedAt; // Hoặc LocalDateTime nếu bạn giữ nguyên kiểu

    // Các trường ID và tên của các mối quan hệ
    private Long employerId;
    private Long categoryId; // <-- Đảm bảo tên là 'categoryId'
    private String categoryName;
    private Long jobLevelId; // <-- Đảm bảo tên là 'jobLevelId'
    private String jobLevelName;
    private Long jobTypeId;  // <-- Đảm bảo tên là 'jobTypeId'
    private String jobTypeName;
}


